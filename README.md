# capacitor-bixolon-printer Template

This is a template for creating Capacitor plugins, specifically designed for Android-only plugins. It includes placeholders that can be automatically replaced with your plugin details.

## Getting Started

1. **Configure your plugin** by editing the `config` object at the top of `init.js`:
   ```javascript
   const config = {
     capacitor-bixolon-printer: 'capacitor-my-awesome-plugin',
     Printer: 'MyAwesomePlugin', 
     com.leeskies.capacitorbixolonprinter: 'com.mycompany.myawesomeplugin',
     A capacitor plugin for communicating with Bixolon printers: 'An awesome Capacitor plugin',
     LeeSkies: 'Your Name',
     https://github.com/LeeSkies/capacitor-bixolon-printer: 'https://github.com/username/capacitor-my-awesome-plugin',
     CapacitorBixolonPrinter: 'CapacitorMyAwesomePlugin',
     capacitorBixolonPrinter: 'capacitorMyAwesomePlugin'
   };
   ```

2. **Initialize the plugin** by running the init script:
   ```bash
   node init.js
   ```

3. **Install dependencies**:
   ```bash
   npm install
   ```

4. **Build the plugin**:
   ```bash
   npm run build
   ```

## Template Structure

- `src/` - TypeScript source files
  - `definitions.ts` - Plugin interface definitions
  - `index.ts` - Main plugin registration
  - `web.ts` - Web implementation (stub for Android-only plugin)
- `android/` - Android native implementation
- `init.js` - Script to replace placeholders with actual plugin details

## Placeholders Used

The template uses the following placeholders that will be replaced by the init script:

- `capacitor-bixolon-printer` - NPM package name
- `Printer` - Plugin class name
- `com.leeskies.capacitorbixolonprinter` - Android package ID (reverse domain notation)
- `A capacitor plugin for communicating with Bixolon printers` - Plugin description
- `LeeSkies` - Author name
- `https://github.com/LeeSkies/capacitor-bixolon-printer` - Git repository URL

## Customization

After running the init script, you'll need to:

1. Add your specific SDK dependencies to `android/build.gradle`
2. Implement the actual functionality in the Android classes
3. Update the TypeScript interfaces as needed for your specific use case

## Testing

- Run Android tests: `npm run verify:android`
- Build verification: `npm run verify:web`
- Lint code: `npm run lint`