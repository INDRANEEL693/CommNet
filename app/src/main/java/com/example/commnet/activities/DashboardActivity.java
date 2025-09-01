package com.example.commnet.activities;

import static com.example.commnet.R.*;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.commnet.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.io.*;
import java.util.*;
public class DashboardActivity extends AppCompatActivity {

    LinearLayout messageLayout;
    ScrollView manageLayout, serverLayout;
    Button btnMessage, btnManage, btnServer;
    EditText editMessage;

    FrameLayout treeContainer; // TreeView container

    // Manage views
    EditText editRollNumber, editCategoryPath;
    RadioGroup roleSelectorManage;
    Button btnAddUser, btnCreateCategory, btnAssignUser;
    Spinner spinnerUsers, spinnerCategories;
    LinearLayout categoryUserContainer;

    // CSV Upload views
    Button btnChooseCsv, btnUploadCsv;
    TextView textCsvPreview;
    private static final int PICK_CSV_REQUEST = 1;
    TreeNode treeRoot;
    private Uri selectedCsvUri;

    // Server views
    EditText editFullName, editRollNo, editDOB, editDomain;
    RadioGroup roleSelectorServer;
    Button btnCreateAccount;

    FirebaseFirestore db;
    AndroidTreeView tView;
    Set<String> selectedUsers = new HashSet<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Hide navigation and status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        db = FirebaseFirestore.getInstance();
        Button btnSendMessage = findViewById(id.btnSendMessage);
        // Navigation buttons
        btnMessage = findViewById(R.id.btnMessage);
        btnManage = findViewById(R.id.btnManage);
        btnServer = findViewById(R.id.btnServer);

        messageLayout = findViewById(R.id.messageLayout);
        manageLayout = findViewById(R.id.manageLayout);
        serverLayout = findViewById(R.id.serverLayout);

        treeContainer = findViewById(R.id.tree_container);

        btnMessage.setOnClickListener(v -> {
            showLayout(messageLayout);
            loadTreeData();
        });
        btnManage.setOnClickListener(v -> showLayout(manageLayout));
        btnServer.setOnClickListener(v -> showLayout(serverLayout));
        editMessage = findViewById(R.id.editMessage);

        // Manage views
        editRollNumber = findViewById(R.id.editRollNumber);
        roleSelectorManage = findViewById(R.id.roleSelectorManage);
        btnAddUser = findViewById(R.id.btnAddUser);
        editCategoryPath = findViewById(R.id.editCategoryPath);
        btnCreateCategory = findViewById(R.id.btnCreateCategory);
        btnAssignUser = findViewById(R.id.btnAssignUser);
        spinnerUsers = findViewById(R.id.spinnerUsers);
        spinnerCategories = findViewById(R.id.spinnerCategories);
        categoryUserContainer = findViewById(R.id.categoryUserContainer);

        // CSV views
        btnChooseCsv = findViewById(R.id.btnChooseCsv);
        btnUploadCsv = findViewById(R.id.btnUploadCsv);
        textCsvPreview = findViewById(R.id.textCsvPreview);

        // Server views
        editFullName = findViewById(R.id.editFullName);
        editRollNo = findViewById(R.id.editRollNo);
        editDOB = findViewById(R.id.editDOB);
        editDomain = findViewById(R.id.editDomain);
        roleSelectorServer = findViewById(R.id.roleSelectorServer);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // Listeners
        btnCreateAccount.setOnClickListener(v -> createAccount());
        btnAddUser.setOnClickListener(v -> addUser());
        btnCreateCategory.setOnClickListener(v -> createCategory());
        btnAssignUser.setOnClickListener(v -> assignUserToCategory());

        btnChooseCsv.setOnClickListener(v -> chooseCsvFile());
        btnUploadCsv.setOnClickListener(v -> uploadCsvToFirestore());

        loadSpinners();
        btnSendMessage.setOnClickListener(v -> {
            String message = editMessage.getText().toString().trim();
            String senderEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            if (message.isEmpty() || selectedUsers.isEmpty()) {
                Toast.makeText(this, "Please select users and enter a message", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> emailList = new ArrayList<>();
            List<String> selected = new ArrayList<>(selectedUsers);

            // Track progress
            final int[] fetchedCount = {0};

            for (String roll : selected) {
                db.collection("users").document(roll).get()
                        .addOnSuccessListener(doc -> {
                            fetchedCount[0]++;
                            if (doc.exists()) {
                                String email = doc.getString("email");
                                if (email != null && !email.isEmpty()) {
                                    emailList.add(email);
                                }
                            }

                            // When all users processed, send message
                            if (fetchedCount[0] == selected.size()) {
                                if (emailList.isEmpty()) {
                                    Toast.makeText(this, "No matching emails found", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Map<String, Object> messageData = new HashMap<>();
                                messageData.put("message", message);
                                messageData.put("users", emailList);
                                messageData.put("timestamp", FieldValue.serverTimestamp());
                                messageData.put("senderEmail", senderEmail);

                                db.collection("sentMessages")
                                        .add(messageData)
                                        .addOnSuccessListener(docRef -> {
                                            Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
                                            editMessage.setText("");
                                            clearSelections(treeRoot);
                                            selectedUsers.clear();
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                                        );
                            }
                        })
                        .addOnFailureListener(e -> {
                            fetchedCount[0]++;
                            if (fetchedCount[0] == selected.size()) {
                                Toast.makeText(this, "Some emails could not be fetched", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });




    }

    private void showLayout(View visibleLayout) {
        messageLayout.setVisibility(View.GONE);
        manageLayout.setVisibility(View.GONE);
        serverLayout.setVisibility(View.GONE);
        visibleLayout.setVisibility(View.VISIBLE);
    }

    // === STEP 4 & 6: Load data, build nodes recursively and initialize TreeView ===
    private void loadTreeData() {
        db.collection("categoryUsers").get().addOnSuccessListener(queryDocumentSnapshots -> {
            Map<String, Node> rootNodesMap = new HashMap<>();

            // Step 1: Build tree of Node objects
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                String categoryPath = doc.getString("category");
                String userField = doc.getString("user");

                if (categoryPath == null || userField == null) continue;

                // Extract user name and roll number
                if (!userField.contains("(")) continue;
                String userName = userField.substring(0, userField.indexOf("(")).trim();
                String userRoll = userField.substring(userField.indexOf("(") + 1, userField.indexOf(")")).trim();

                String[] parts = categoryPath.split("_");
                Node current = rootNodesMap.get(parts[0]);
                if (current == null) {
                    current = new Node(parts[0], false);
                    rootNodesMap.put(parts[0], current);
                }

                for (int i = 1; i < parts.length; i++) {
                    Node child = findChildByName(current.children, parts[i]);
                    if (child == null) {
                        child = new Node(parts[i], false);
                        current.children.add(child);
                    }
                    current = child;
                }

                // Now add user node with name and roll
                Node userNode = new Node(userName, userRoll, true);
                current.children.add(userNode);
            }

            // Step 2: Build TreeNode structure from Node objects
            TreeNode root = TreeNode.root();
            for (Node node : rootNodesMap.values()) {
                root.addChild(buildTree(node));
            }

            // Step 3: Attach TreeView to container
            AndroidTreeView tView = new AndroidTreeView(this, root);
            tView.setDefaultAnimation(true);
            tView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
            treeContainer.removeAllViews();
            treeContainer.addView(tView.getView());

            // Optional: Save reference if needed for clearing later
            treeRoot = root;

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load tree: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }


    // Helper method to find child node by name in list
    private Node findChildByName(List<Node> children, String name) {
        for (Node child : children) {
            if (child.name.equals(name)) {
                return child;
            }
        }
        return null;
    }

    // Recursive method to build TreeNode from Node
    private TreeNode buildTree(Node rootNode) {
        TreeNode treeNode;

        if (rootNode.isUser) {
            // This user node will use the roll number to track selections
            treeNode = new TreeNode(rootNode).setViewHolder(
                    new UserViewHolder(this, (roll, isChecked) -> {
                        if (isChecked) {
                            selectedUsers.add(roll);
                        } else {
                            selectedUsers.remove(roll);
                        }
                    })
            );
        } else {
            // This is a category node
            treeNode = new TreeNode(rootNode).setViewHolder(new CategoryViewHolder(this));

            // Recursively add children
            for (Node child : rootNode.children) {
                treeNode.addChild(buildTree(child));
            }
        }

        return treeNode;
    }


    // === Other existing methods ===

    private void createAccount() {
        String name = editFullName.getText().toString().trim();
        String roll = editRollNo.getText().toString().trim();
        String dob = editDOB.getText().toString().trim();
        String domain = editDomain.getText().toString().trim();
        String email = roll + "@" + domain;
        String password = dob.replace("-", "") + roll;

        int selectedId = roleSelectorServer.getCheckedRadioButtonId();
        String role = selectedId == -1 ? "User" : ((RadioButton) findViewById(selectedId)).getText().toString();

        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("roll", roll);
        user.put("dob", dob);
        user.put("email", email);
        user.put("password", password);
        user.put("role", role);

        db.collection("users").document(roll).set(user)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "User created", Toast.LENGTH_SHORT).show();
                    loadSpinners();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to create user", Toast.LENGTH_SHORT).show());
    }

    private void addUser() {
        String roll = editRollNumber.getText().toString().trim();
        int selectedId = roleSelectorManage.getCheckedRadioButtonId();
        String role = selectedId == -1 ? "User" : ((RadioButton) findViewById(selectedId)).getText().toString();

        db.collection("users").document(roll).update("role", role)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "User added/updated", Toast.LENGTH_SHORT).show();
                    loadSpinners();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
    }

    private void createCategory() {
        String rawPath = editCategoryPath.getText().toString().trim();

        if (rawPath.isEmpty()) {
            Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate characters
        if (rawPath.contains(".") || rawPath.contains("#") || rawPath.contains("$") || rawPath.contains("[") || rawPath.contains("]")) {
            Toast.makeText(this, "Invalid characters in category name", Toast.LENGTH_LONG).show();
            return;
        }

        // Convert slashes to underscores to form flat Firestore-friendly ID
        String firestorePath = rawPath.replace("/", "_");

        Map<String, Object> data = new HashMap<>();
        data.put("path", firestorePath);
        data.put("displayPath", rawPath); // Optional: keep original for UI

        db.collection("categories").document(firestorePath)
                .set(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Category created: " + firestorePath, Toast.LENGTH_SHORT).show();
                    editCategoryPath.setText("");
                    loadSpinners(); // refresh UI
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create category: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }


    private void assignUserToCategory() {
        String user = spinnerUsers.getSelectedItem().toString();
        String category = spinnerCategories.getSelectedItem().toString();

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("category", category);

        db.collection("categoryUsers").add(data)
                .addOnSuccessListener(unused -> Toast.makeText(this, "User assigned", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
    }

    private void loadSpinners() {
        db.collection("users").get().addOnSuccessListener(qs -> {
            List<String> users = new ArrayList<>();
            for (DocumentSnapshot doc : qs) {
                String name = doc.getString("name");
                if (name == null) name = doc.getId();
                users.add(name + " (" + doc.getId() + ")");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, users);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerUsers.setAdapter(adapter);
        });

        db.collection("categories").get().addOnSuccessListener(qs -> {
            List<String> categories = new ArrayList<>();
            for (DocumentSnapshot doc : qs) {
                categories.add(doc.getId());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategories.setAdapter(adapter);
        });
    }

    private void chooseCsvFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select CSV File"), PICK_CSV_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CSV_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedCsvUri = data.getData();
            if (selectedCsvUri != null) {
                previewCsvFile(selectedCsvUri);
            }
        }
    }

    private void previewCsvFile(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder preview = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                preview.append(line).append("\n");
            }
            textCsvPreview.setText(preview.toString());

        } catch (IOException e) {
            e.printStackTrace();
            textCsvPreview.setText("Failed to read file.");
        }
    }

    private void uploadCsvToFirestore() {
        if (selectedCsvUri == null) {
            Toast.makeText(this, "No CSV file selected", Toast.LENGTH_SHORT).show();
            return;
        }

        try (InputStream inputStream = getContentResolver().openInputStream(selectedCsvUri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                String name = parts[0].trim();
                String roll = parts[1].trim();
                String dob = parts[2].trim();
                String domain = parts[3].trim();

                String email = roll + "@" + domain;
                String password = dob.replace("-", "") + roll;

                Map<String, Object> user = new HashMap<>();
                user.put("name", name);
                user.put("roll", roll);
                user.put("dob", dob);
                user.put("email", email);
                user.put("password", password);
                user.put("role", "User");

                db.collection("users").document(roll).set(user);
            }

            Toast.makeText(this, "CSV users uploaded", Toast.LENGTH_SHORT).show();
            loadSpinners();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "CSV upload failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearSelections(TreeNode root) {
        if (root == null) return;

        for (TreeNode child : root.getChildren()) {
            clearSelections(child);
        }

        root.setSelected(false);
        TreeNode.BaseNodeViewHolder holder = root.getViewHolder();

        if (holder != null) {
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).updateCheckbox(false);
        } else if (holder instanceof CategoryViewHolder) {
            ((CategoryViewHolder) holder).updateCheckbox(false);
        }}
    }
}
