/**
 * Migration script: move notes stored in flat layout
 *  /users/{account}/notes/{noteId}
 * to device-grouped layout:
 *  /users/{account}/notes/{deviceId}/{noteId}
 *
 * Usage:
 * 1) Place your service account JSON and pass its path with --serviceAccount (or set GOOGLE_APPLICATION_CREDENTIALS env var)
 * 2) Install dependencies: npm install firebase-admin minimist
 * 3) Run in dry-run (no writes): node migrateFlatNotes.js --dryRun=true
 * 4) To apply changes: node migrateFlatNotes.js --dryRun=false --serviceAccount=./serviceAccountKey.json
 */

const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');
const argv = require('minimist')(process.argv.slice(2));

const dryRun = argv.dryRun === undefined ? true : (argv.dryRun === 'true' || argv.dryRun === true);
const serviceAccountPath = argv.serviceAccount || process.env.GOOGLE_APPLICATION_CREDENTIALS;

if (!serviceAccountPath) {
  console.error('Provide --serviceAccount=path/to/serviceAccount.json or set GOOGLE_APPLICATION_CREDENTIALS');
  process.exit(1);
}

if (!fs.existsSync(serviceAccountPath)) {
  console.error('Service account file not found at', serviceAccountPath);
  process.exit(1);
}

const serviceAccount = require(path.resolve(serviceAccountPath));

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: serviceAccount.databaseURL || undefined
});

const db = admin.database();

async function main() {
  console.log('Migration started. dryRun =', dryRun);

  const usersSnap = await db.ref('users').get();
  if (!usersSnap.exists()) {
    console.log('No users node found. Exiting.');
    return;
  }

  let totalMoved = 0;

  for (const [accountId, userNode] of Object.entries(usersSnap.val())) {
    if (!userNode || typeof userNode !== 'object') continue;
    const notesNode = userNode.notes;
    if (!notesNode) continue;

    // Heuristic: if notesNode has children that themselves have children that look like notes grouped by device,
    // skip (already migrated)
    const firstChildKey = Object.keys(notesNode)[0];
    const firstChild = notesNode[firstChildKey];

    // If firstChild appears to be a note (has title/description or mymobiledeviceid), then it's flat
    const looksFlat = firstChild && (firstChild.title !== undefined || firstChild.description !== undefined || firstChild.mymobiledeviceid !== undefined);

    if (!looksFlat) {
      // Probably already grouped by device; skip
      continue;
    }

    console.log(`Found flat notes for account: ${accountId}. Preparing migration of ${Object.keys(notesNode).length} notes.`);

    // Build multi-path update
    const updates = {};
    for (const [noteId, noteValue] of Object.entries(notesNode)) {
      try {
        // noteValue may be an object with encrypted title/description and mymobiledeviceid
        const deviceId = (noteValue && noteValue.mymobiledeviceid) ? noteValue.mymobiledeviceid : 'unknown-device';
        const targetPath = `/users/${accountId}/notes/${deviceId}/${noteId}`;
        const sourcePath = `/users/${accountId}/notes/${noteId}`;

        updates[targetPath] = noteValue;
        updates[sourcePath] = null; // delete old
        totalMoved++;
      } catch (e) {
        console.warn('Skipping note', noteId, 'due to error', e.message);
      }
    }

    if (Object.keys(updates).length === 0) continue;

    if (dryRun) {
      console.log(`Dry run: would apply ${Object.keys(updates).length} path updates for account ${accountId}`);
    } else {
      console.log(`Applying updates for account ${accountId}...`);
      await db.ref().update(updates);
      console.log(`Applied updates for account ${accountId}`);
    }
  }

  console.log('Migration finished. totalMoved (counted source notes):', totalMoved);
}

main().then(() => process.exit(0)).catch(err => {
  console.error('Migration error', err);
  process.exit(1);
});

