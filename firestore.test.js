const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const DATABASE_ID = "ai-studio-android-webforge-2f5f3677-3f08-4192-8579-dc3fa4d38870";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProjectPayload(uid, overrides = {}) {
  const { serverTimestamp } = require("firebase/firestore");
  return {
    userId: uid,
    title: "SaaS Analytics Pro",
    description: "Landing page SaaS moderne",
    category: "saas",
    htmlCode: "<html><body><h1>Hello</h1></body></html>",
    cssCode: "body { color: #fff; }",
    jsCode: "console.log('ready');",
    versionCount: 1,
    isFavorite: false,
    createdAt: new Date(),
    updatedAt: new Date(),
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot list or create projects", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore(DATABASE_ID);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("projects").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj1").set({
      userId: ALICE_UID,
      title: "Hack",
    })
  );
});

test("Owner can create, read, update, and delete valid project", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const projRef = aliceDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_1");

  await assertSucceeds(
    projRef.set({
      userId: ALICE_UID,
      title: "Agence Web Pro",
      description: "Site complet",
      category: "saas",
      htmlCode: "<h1>Studio</h1>",
      cssCode: "h1{color:red}",
      jsCode: "",
      versionCount: 1,
      isFavorite: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(projRef.get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("projects").get());

  await assertSucceeds(
    projRef.update({
      title: "Agence Web Pro v2",
      versionCount: 2,
      isFavorite: true,
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(projRef.delete());
});

test("Cross-user access is denied", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore(DATABASE_ID);
    await adminDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_alice").set({
      userId: ALICE_UID,
      title: "Alice Site",
      description: "Private",
      category: "landing",
      htmlCode: "<h1>Alice</h1>",
      cssCode: "",
      jsCode: "",
      versionCount: 1,
      isFavorite: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore(DATABASE_ID);
  const aliceProjRef = bobDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_alice");

  await assertFails(aliceProjRef.get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("projects").get());
  await assertFails(aliceProjRef.update({ title: "Hacked", updatedAt: new Date() }));
  await assertFails(aliceProjRef.delete());
});

test("Shadow update with ghost field and userId spoofing are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const projRef = aliceDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_shadow");

  // Spoofed userId
  await assertFails(
    projRef.set({
      userId: BOB_UID,
      title: "Spoof",
      description: "Desc",
      category: "saas",
      htmlCode: "<h1>Hi</h1>",
      cssCode: "",
      jsCode: "",
      versionCount: 1,
      isFavorite: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Ghost field
  await assertFails(
    projRef.set({
      userId: ALICE_UID,
      title: "Ghost",
      description: "Desc",
      category: "saas",
      htmlCode: "<h1>Hi</h1>",
      cssCode: "",
      jsCode: "",
      versionCount: 1,
      isFavorite: false,
      isAdmin: true,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Valid create, then shadow update
  await assertSucceeds(
    projRef.set({
      userId: ALICE_UID,
      title: "Valid",
      description: "Desc",
      category: "saas",
      htmlCode: "<h1>Hi</h1>",
      cssCode: "",
      jsCode: "",
      versionCount: 1,
      isFavorite: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertFails(
    projRef.update({
      title: "Valid 2",
      ghostField: "bad",
      updatedAt: new Date(),
    })
  );
});

test("Messages and versions require existing parent project and reject updates", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const projRef = aliceDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_parent");
  const msgRef = projRef.collection("messages").doc("msg_1");
  const verRef = projRef.collection("versions").doc("ver_1");

  // Orphaned message/version must fail
  await assertFails(
    msgRef.set({
      userId: ALICE_UID,
      projectId: "proj_parent",
      role: "user",
      content: "Créer un site",
      summary: "",
      versionTag: 1,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    projRef.set({
      userId: ALICE_UID,
      title: "Parent",
      description: "Desc",
      category: "landing",
      htmlCode: "<h1>Parent</h1>",
      cssCode: "",
      jsCode: "",
      versionCount: 1,
      isFavorite: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    msgRef.set({
      userId: ALICE_UID,
      projectId: "proj_parent",
      role: "user",
      content: "Créer un site",
      summary: "",
      versionTag: 1,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    verRef.set({
      userId: ALICE_UID,
      projectId: "proj_parent",
      versionNumber: 1,
      promptLabel: "Initial build",
      htmlCode: "<h1>Parent</h1>",
      cssCode: "",
      jsCode: "",
      createdAt: new Date(),
    })
  );

  // Updates to immutable messages/versions must fail
  await assertFails(msgRef.update({ content: "Tampered" }));
  await assertFails(verRef.update({ htmlCode: "<h1>Tampered</h1>" }));
});
