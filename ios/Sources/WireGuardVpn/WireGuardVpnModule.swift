import Foundation
import React
import NetworkExtension

@objc(WireGuardVpnModule)
class WireGuardVpnModule: NSObject {

  // MARK: – React Native Module Setup

  @objc
  static func requiresMainQueueSetup() -> Bool {
    // No UI, so we can initialize off the main thread.
    return false
  }

  // MARK: – Public API

  /**
   Initialize the VPN backend. Must be called before any other method.
   */
  @objc(initialize:rejecter:)
  func initialize(_ resolve: @escaping RCTPromiseResolveBlock,
                  rejecter reject: @escaping RCTPromiseRejectBlock) {
    do {
      // TODO: Request permissions or set up any services before connecting.
      resolve(nil)
    } catch let error {
      reject("INITIALIZE_ERROR", "Failed to initialize VPN backend: \(error.localizedDescription)", error)
    }
  }

  /**
   Establish a WireGuard tunnel using the given configuration.
   */
  @objc(connect:resolver:rejecter:)
  func connect(_ config: NSDictionary,
               resolver resolve: @escaping RCTPromiseResolveBlock,
               rejecter reject: @escaping RCTPromiseRejectBlock) {
    // Validate mandatory fields
    guard let privateKey   = config["privateKey"] as? String, !privateKey.isEmpty else {
      reject("INVALID_CONFIG", "Missing or empty 'privateKey'", nil); return
    }
    guard let publicKey    = config["publicKey"] as? String, !publicKey.isEmpty else {
      reject("INVALID_CONFIG", "Missing or empty 'publicKey'", nil); return
    }
    guard let serverAddr   = config["serverAddress"] as? String, !serverAddr.isEmpty else {
      reject("INVALID_CONFIG", "Missing or empty 'serverAddress'", nil); return
    }
    guard let serverPortNS = config["serverPort"] as? NSNumber else {
      reject("INVALID_CONFIG", "Missing or invalid 'serverPort'", nil); return
    }
    let serverPort = serverPortNS.intValue

    guard let allowedIPs = config["allowedIPs"] as? [String], !allowedIPs.isEmpty else {
      reject("INVALID_CONFIG", "Missing or empty 'allowedIPs'", nil); return
    }
    guard let dns = config["dns"] as? [String], !dns.isEmpty else {
      reject("INVALID_CONFIG", "Missing or empty 'dns'", nil); return
    }

    // Optional fields
    let mtu = (config["mtu"] as? NSNumber)?.intValue ?? 1420
    let presharedKey = config["presharedKey"] as? String

    do {
      // TODO: Build NEPacketTunnelProviderProtocol with these settings
      //       and start the VPN tunnel.
      resolve(nil)
    } catch let error {
      reject("CONNECT_ERROR", "Error starting VPN: \(error.localizedDescription)", error)
    }
  }

  /**
   Tear down the active WireGuard tunnel.
   */
  @objc(disconnect:rejecter:)
  func disconnect(_ resolve: @escaping RCTPromiseResolveBlock,
                  rejecter reject: @escaping RCTPromiseRejectBlock) {
    do {
      // TODO: Stop the NEPacketTunnelProvider tunnel.
      resolve(nil)
    } catch let error {
      reject("DISCONNECT_ERROR", "Error stopping VPN: \(error.localizedDescription)", error)
    }
  }

  /**
   Retrieve the current tunnel status.
   */
  @objc(getStatus:rejecter:)
  func getStatus(_ resolve: @escaping RCTPromiseResolveBlock,
                 rejecter reject: @escaping RCTPromiseRejectBlock) {
    do {
      // TODO: Query your tunnel provider for actual status.
      // Valid values match WireGuardStatus enum in JS.
      let status = "disconnected"
      resolve(status)
    } catch let error {
      reject("STATUS_ERROR", "Error fetching VPN status: \(error.localizedDescription)", error)
    }
  }

  /**
   Check if WireGuard is supported on this device.
   */
  @objc(isDeviceSupported:rejecter:)
  func isDeviceSupported(_ resolve: @escaping RCTPromiseResolveBlock,
                         rejecter reject: @escaping RCTPromiseRejectBlock) {
    do {
      // A simple check: can we load NEVPNManager?
      let supported = NSClassFromString("NEVPNManager") != nil
      resolve(supported)
    } catch let error {
      reject("SUPPORTED_ERROR", "Error checking support: \(error.localizedDescription)", error)
    }
  }
}
