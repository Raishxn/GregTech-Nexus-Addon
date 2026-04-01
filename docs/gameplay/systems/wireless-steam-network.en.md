# :satellite: Wireless Steam Network
## How it Works
Each player has their own wireless steam network. Uses `SteamNetworkData` (SavedData) to store steam globally.

### Components
- **Wireless Steam Output Hatch** - Connect to boiler, sends steam to the network
- **Wireless Steam Input Hatch** - Connect to machine, receives steam from the network

### Features
- **No distance limit** (works across dimensions!)
- Each player has a separate network (UUID-based)
- Steel variants have maximum capacity (Integer.MAX_VALUE)

### Tips
- Switch to Steel Wireless Hatches as soon as possible
- One Mega Solar Boiler with Wireless Output can power your entire factory
