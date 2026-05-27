#!/usr/bin/env node
/**
 * generate-icons.js
 * Generates all Android icon variants from assets/images/icon.png
 *
 * Usage:  node scripts/generate-icons.js
 * Deps:   npm install sharp
 */

const sharp  = require('sharp');
const fs     = require('fs');
const path   = require('path');

// ─── Config ──────────────────────────────────────────────────────────────────

const SRC      = path.resolve(__dirname, '../assets/images/icon.png');
const RES      = path.resolve(__dirname, '../android/app/src/main/res');
const BG_COLOR = '#0F4334';

// Launcher icon sizes per density  (dp base = 48)
const LAUNCHER_DENSITIES = {
  'mipmap-mdpi':    48,
  'mipmap-hdpi':    72,
  'mipmap-xhdpi':   96,
  'mipmap-xxhdpi':  144,
  'mipmap-xxxhdpi': 192,
};

// Adaptive foreground sizes (same folders)
const FOREGROUND_DENSITIES = LAUNCHER_DENSITIES;

// Notification + splash icon sizes per density  (dp base = 24 / 200)
const DRAWABLE_DENSITIES = {
  'drawable-mdpi':    { notif: 24,  splash: 200 },
  'drawable-hdpi':    { notif: 36,  splash: 300 },
  'drawable-xhdpi':   { notif: 48,  splash: 400 },
  'drawable-xxhdpi':  { notif: 72,  splash: 600 },
  'drawable-xxxhdpi': { notif: 96,  splash: 800 },
};

// XML files that must start with <?xml …?> on line 1
const XML_FILES = [
  'values/ic_launcher_background.xml',
  'mipmap-anydpi-v26/ic_launcher.xml',
  'mipmap-anydpi-v26/ic_launcher_round.xml',
];

// ─── Helpers ─────────────────────────────────────────────────────────────────

function ensureDir(dir) {
  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
}

function hexToRgb(hex) {
  const h = hex.replace('#', '');
  return {
    r: parseInt(h.slice(0, 2), 16),
    g: parseInt(h.slice(2, 4), 16),
    b: parseInt(h.slice(4, 6), 16),
  };
}

/** Solid colour canvas as a sharp buffer */
function solidCanvas(size, hex) {
  const { r, g, b } = hexToRgb(hex);
  return sharp({
    create: { width: size, height: size, channels: 3, background: { r, g, b } },
  }).png().toBuffer();
}

/** Circular SVG mask for round icons */
function circleMask(size) {
  const r = size / 2;
  return Buffer.from(
    `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}">` +
    `<circle cx="${r}" cy="${r}" r="${r}" fill="white"/>` +
    `</svg>`
  );
}

// ─── Icon generators ─────────────────────────────────────────────────────────

/**
 * Square launcher icon  (icon centred at 75 % of canvas, solid BG)
 */
async function makeLauncherIcon(size) {
  const iconSize = Math.round(size * 0.75);
  const offset   = Math.round((size - iconSize) / 2);

  const bg   = await solidCanvas(size, BG_COLOR);
  const icon = await sharp(SRC)
    .resize(iconSize, iconSize, { fit: 'contain', background: { r: 0, g: 0, b: 0, alpha: 0 } })
    .toBuffer();

  return sharp(bg)
    .composite([{ input: icon, left: offset, top: offset }])
    .png()
    .toBuffer();
}

/**
 * Round launcher icon  (same as square but clipped to a circle)
 */
async function makeLauncherRoundIcon(size) {
  const square = await makeLauncherIcon(size);
  const mask   = circleMask(size);

  return sharp(square)
    .composite([{ input: mask, blend: 'dest-in' }])
    .png()
    .toBuffer();
}

/**
 * Adaptive icon foreground  (icon at 66 % of canvas = safe zone, transparent BG)
 */
async function makeForegroundIcon(size) {
  const iconSize = Math.round(size * 0.66);
  const offset   = Math.round((size - iconSize) / 2);

  const icon = await sharp(SRC)
    .resize(iconSize, iconSize, { fit: 'contain', background: { r: 0, g: 0, b: 0, alpha: 0 } })
    .toBuffer();

  return sharp({
    create: { width: size, height: size, channels: 4, background: { r: 0, g: 0, b: 0, alpha: 0 } },
  })
    .composite([{ input: icon, left: offset, top: offset }])
    .png()
    .toBuffer();
}

/**
 * Notification icon  (white silhouette on transparent)
 * Pipeline: resize → greyscale → threshold → map: bright pixel = white opaque, dark = transparent
 */
async function makeNotificationIcon(size) {
  const { data, info } = await sharp(SRC)
    .resize(size, size, { fit: 'contain', background: { r: 0, g: 0, b: 0, alpha: 0 } })
    .greyscale()
    .threshold(80)   // tune 0–255: lower keeps more of the icon
    .ensureAlpha()
    .raw()
    .toBuffer({ resolveWithObject: true });

  const out = Buffer.alloc(info.width * info.height * 4);
  for (let i = 0; i < info.width * info.height; i++) {
    const gray = data[i * 4];  // R channel (greyscale → all channels equal)
    out[i * 4]     = 255;      // R white
    out[i * 4 + 1] = 255;      // G white
    out[i * 4 + 2] = 255;      // B white
    out[i * 4 + 3] = gray;     // A: 255 = opaque where bright, 0 = transparent where dark
  }

  return sharp(out, { raw: { width: info.width, height: info.height, channels: 4 } })
    .png()
    .toBuffer();
}

/**
 * Splash screen logo  (icon centred, transparent BG)
 */
async function makeSplashLogo(size) {
  const icon = await sharp(SRC)
    .resize(size, size, { fit: 'contain', background: { r: 0, g: 0, b: 0, alpha: 0 } })
    .toBuffer();

  return sharp({
    create: { width: size, height: size, channels: 4, background: { r: 0, g: 0, b: 0, alpha: 0 } },
  })
    .composite([{ input: icon }])
    .png()
    .toBuffer();
}

// ─── XML fixer ────────────────────────────────────────────────────────────────

/**
 * Ensures <?xml version="1.0" encoding="utf-8"?> is always the very first line.
 * Removes any content (e.g. Apache licence comments) that precedes it.
 */
function fixXmlDeclaration(filePath) {
  if (!fs.existsSync(filePath)) return;

  const original = fs.readFileSync(filePath, 'utf8');
  const decl     = '<?xml version="1.0" encoding="utf-8"?>';
  const declIdx  = original.indexOf('<?xml');

  if (declIdx === -1) {
    // No declaration at all — prepend one
    fs.writeFileSync(filePath, decl + '\n' + original.trimStart(), 'utf8');
    console.log(`  [XML] prepended declaration → ${path.relative(RES, filePath)}`);
    return;
  }

  if (declIdx === 0) {
    // Already correct
    return;
  }

  // Declaration exists but something precedes it — strip the leading content
  const fixed = original.slice(declIdx);
  fs.writeFileSync(filePath, fixed, 'utf8');
  console.log(`  [XML] fixed declaration position → ${path.relative(RES, filePath)}`);
}

// ─── Main ─────────────────────────────────────────────────────────────────────

async function run() {
  if (!fs.existsSync(SRC)) {
    console.error(`Source not found: ${SRC}`);
    process.exit(1);
  }

  console.log('Generating Android icons from:', SRC);
  console.log('Output res dir:               ', RES);
  console.log('Background colour:            ', BG_COLOR);
  console.log('');

  // ── Launcher icons ──────────────────────────────────────────────────────────
  console.log('▸ Launcher icons (ic_launcher + ic_launcher_round)');
  for (const [folder, size] of Object.entries(LAUNCHER_DENSITIES)) {
    const dir = path.join(RES, folder);
    ensureDir(dir);

    const square = await makeLauncherIcon(size);
    fs.writeFileSync(path.join(dir, 'ic_launcher.png'), square);

    const round = await makeLauncherRoundIcon(size);
    fs.writeFileSync(path.join(dir, 'ic_launcher_round.png'), round);

    console.log(`  ${folder.padEnd(22)} ${size}×${size}px`);
  }

  // ── Adaptive foreground ──────────────────────────────────────────────────────
  console.log('\n▸ Adaptive foreground (ic_launcher_foreground)');
  for (const [folder, size] of Object.entries(FOREGROUND_DENSITIES)) {
    const dir = path.join(RES, folder);
    ensureDir(dir);

    const fg = await makeForegroundIcon(size);
    fs.writeFileSync(path.join(dir, 'ic_launcher_foreground.png'), fg);

    console.log(`  ${folder.padEnd(22)} ${size}×${size}px`);
  }

  // ── Notification icons ───────────────────────────────────────────────────────
  console.log('\n▸ Notification icons (ic_notification)');
  for (const [folder, { notif }] of Object.entries(DRAWABLE_DENSITIES)) {
    const dir = path.join(RES, folder);
    ensureDir(dir);

    const notifIcon = await makeNotificationIcon(notif);
    fs.writeFileSync(path.join(dir, 'ic_notification.png'), notifIcon);

    console.log(`  ${folder.padEnd(22)} ${notif}×${notif}px`);
  }

  // ── Splash screen logo ───────────────────────────────────────────────────────
  console.log('\n▸ Splash screen logos (splashscreen_logo)');
  for (const [folder, { splash }] of Object.entries(DRAWABLE_DENSITIES)) {
    const dir = path.join(RES, folder);
    ensureDir(dir);

    const logo = await makeSplashLogo(splash);
    fs.writeFileSync(path.join(dir, 'splashscreen_logo.png'), logo);

    console.log(`  ${folder.padEnd(22)} ${splash}×${splash}px`);
  }

  // ── XML fixes ────────────────────────────────────────────────────────────────
  console.log('\n▸ XML declaration fixes');
  for (const rel of XML_FILES) {
    fixXmlDeclaration(path.join(RES, rel));
  }

  console.log('\n✓ Done.');
}

run().catch(err => {
  console.error(err);
  process.exit(1);
});
