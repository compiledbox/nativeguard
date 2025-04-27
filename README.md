# NativeGuard

> A React Native native-module for WireGuard VPN integration on Android & iOS

[![npm version](https://img.shields.io/npm/v/nativeguard.svg)](https://www.npmjs.com/package/nativeguard)  
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 🚀 Features

- **Initialize** the native VPN engine  
- **Connect** / **Disconnect** WireGuard tunnels  
- **Get status** of the tunnel (`disconnected`, `connecting`, `connected`, …)  
- **Check device support** (kernel/API availability)  
- **TypeScript-first** with full typings and `WireGuardError` for rich error codes  

---

## 🛠 Installation

### 1. Install the package

```bash
npm install nativeguard
# or with yarn
yarn add nativeguard
```

## Usage

```typescript

import {
  initialize,
  connect,
  disconnect,
  getStatus,
  isDeviceSupported,
  WireGuardStatus,
} from 'nativeguard';
import type { WireGuardConfig } from 'nativeguard';

const config: WireGuardConfig = {
  privateKey:    'YOUR_CLIENT_PRIVATE_KEY',
  publicKey:     'YOUR_SERVER_PUBLIC_KEY',
  serverAddress: 'your.server.ip.or.hostname',
  serverPort:    51820,
  allowedIPs:    ['0.0.0.0/0'],
  dns:           ['1.1.1.1'],
  mtu:           1420,             // optional, defaults to 1420
  // presharedKey: 'OPTIONAL_PRESHARED_KEY',
};

async function runVpnFlow() {
  try {
    // 1. Initialize the native engine
    await initialize();

    // 2. Check support
    if (!(await isDeviceSupported())) {
      console.warn('WireGuard not supported on this device.');
      return;
    }

    // 3. Connect
    await connect(config);

    // 4. Poll status
    let status = await getStatus();
    console.log('Tunnel status:', status);
    if (status === WireGuardStatus.CONNECTED) {
      console.log('✅ Tunnel is up!');
    }

    // 5. Disconnect when done
    await disconnect();
    console.log('🔌 Tunnel stopped');
  } catch (err) {
    console.error('VPN error:', err);
  }
}

```
## License
This project is licensed under the MIT License.