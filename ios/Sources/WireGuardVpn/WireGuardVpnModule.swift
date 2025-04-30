import Foundation
import React
import NetworkExtension

@objc(WireGuardVpnModule)
class WireGuardVpnModule: NSObject {

  @objc
  static func requiresMainQueueSetup() -> Bool { false }

  private let manager = NETunnelProviderManager()

  // MARK: – Initialize (load existing or create new manager)

  @objc(initialize:rejecter:)
  func initialize(_ resolve: @escaping RCTPromiseResolveBlock,
                  rejecter reject: @escaping RCTPromiseRejectBlock) {
    NETunnelProviderManager.loadAllFromPreferences { [weak self] managers, error in
      guard let self = self else { return }
      if let error = error {
        reject("INITIALIZE_ERROR", "Failed to load VPN preferences: \(error)", error)
        return
      }
      if let existing = managers?.first {
        self.manager = existing
      } else {
        self.manager = NETunnelProviderManager()
      }
      resolve(nil)
    }
  }

  // MARK: – Connect (install/update config & start)

  @objc(connect:resolver:rejecter:)
  func connect(_ config: NSDictionary,
               resolver resolve: @escaping RCTPromiseResolveBlock,
               rejecter reject: @escaping RCTPromiseRejectBlock) {
    // Build the WireGuard .conf text
    guard let configText = buildConfigText(config) else {
      reject("INVALID_CONFIG", "Missing required WireGuard fields", nil)
      return
    }

    // Create a tunnel protocol
    let proto = NETunnelProviderProtocol()
    proto.providerBundleIdentifier = Bundle.main.bundleIdentifier! + ".wgExtension"
    proto.serverAddress = "WireGuard"  // not used for WG, but required
    proto.providerConfiguration = ["wgConfig": configText]

    manager.protocolConfiguration = proto
    manager.localizedDescription = "NativeGuard Tunnel"
    manager.isEnabled = true

    // Save to preferences
    manager.saveToPreferences { [weak self] error in
      if let error = error {
        reject("CONNECT_ERROR", "Failed to save VPN config: \(error)", error)
        return
      }
      // Start the tunnel
      do {
        try self?.manager.connection.startVPNTunnel()
        resolve(nil)
      } catch let err {
        reject("CONNECT_ERROR", "Failed to start tunnel: \(err)", err)
      }
    }
  }

  // MARK: – Disconnect

  @objc(disconnect:rejecter:)
  func disconnect(_ resolve: @escaping RCTPromiseResolveBlock,
                  rejecter reject: @escaping RCTPromiseRejectBlock) {
    manager.connection.stopVPNTunnel()
    resolve(nil)
  }

  // MARK: – Status

  @objc(getStatus:rejecter:)
  func getStatus(_ resolve: @escaping RCTPromiseResolveBlock,
                 rejecter reject: @escaping RCTPromiseRejectBlock) {
    let rawStatus = manager.connection.status
    let statusString: String
    switch rawStatus {
    case .invalid:        statusString = "invalid"
    case .disconnected:   statusString = "disconnected"
    case .connecting:     statusString = "connecting"
    case .connected:      statusString = "connected"
    case .reasserting:    statusString = "reasserting"
    case .disconnecting:  statusString = "disconnecting"
    @unknown default:     statusString = "unknown"
    }
    resolve(statusString)
  }

  // MARK: – Support check

  @objc(isDeviceSupported:rejecter:)
  func isDeviceSupported(_ resolve: @escaping RCTPromiseResolveBlock,
                         rejecter reject: @escaping RCTPromiseRejectBlock) {
    // NEVPNManager is available on iOS 9+
    let supported = ProcessInfo.processInfo.operatingSystemVersion.majorVersion >= 9
    resolve(supported)
  }

  // MARK: – Helpers

  private func buildConfigText(_ cfg: NSDictionary) -> String? {
    guard
      let priv = cfg["privateKey"] as? String,
      let pub  = cfg["publicKey"]  as? String,
      let addr = cfg["serverAddress"] as? String,
      let port = cfg["serverPort"] as? NSNumber,
      let allowed = cfg["allowedIPs"] as? [String],
      let dns   = cfg["dns"] as? [String]
    else { return nil }

    let mtu = (cfg["mtu"] as? NSNumber)?.intValue ?? 1420
    let ps  = cfg["presharedKey"] as? String

    return """
    [Interface]
    PrivateKey = \(priv)
    MTU = \(mtu)

    [Peer]
    PublicKey = \(pub)
    Endpoint = \(addr):\(port.intValue)
    AllowedIPs = \(allowed.joined(separator: ","))
    DNS = \(dns.joined(separator: ","))
    \(ps.map { "PresharedKey = \($0)" } ?? "")
    """
  }
}
