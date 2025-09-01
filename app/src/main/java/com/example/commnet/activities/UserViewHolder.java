package com.example.commnet.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.commnet.R;
import com.unnamed.b.atv.model.TreeNode;

import java.util.function.BiConsumer;

public class UserViewHolder extends TreeNode.BaseNodeViewHolder<Node> {
    private CheckBox checkBox;
    private BiConsumer<String, Boolean> selectionCallback;

    public UserViewHolder(Context context, BiConsumer<String, Boolean> selectionCallback) {
        super(context);
        this.selectionCallback = selectionCallback;
    }

    @Override
    public View createNodeView(TreeNode node, Node value) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.tree_node_user, null, false);

        TextView textView = view.findViewById(R.id.userText);
        checkBox = view.findViewById(R.id.user_checkbox);

        // Display name and roll number
        if (value.roll != null && !value.roll.isEmpty()) {
            textView.setText("👤 " + value.name + " (" + value.roll + ")");
        } else {
            textView.setText("👤 " + value.name);
        }

        int depth = node.getLevel();
        int paddingLeft = depth * 50;
        view.setPadding(paddingLeft, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());

        checkBox.setChecked(node.isSelected());

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            node.setSelected(isChecked);
            if (selectionCallback != null && value.roll != null) {
                selectionCallback.accept(value.roll, isChecked);  // ✅ send roll
            }
            updateParentSelection(node.getParent());
        });

        return view;
    }

    public void updateCheckbox(boolean isChecked) {
        if (checkBox != null) checkBox.setChecked(isChecked);
    }

    private void updateParentSelection(TreeNode parent) {
        if (parent == null) return;

        boolean allSelected = true;
        for (TreeNode child : parent.getChildren()) {
            if (!child.isSelected()) {
                allSelected = false;
                break;
            }
        }

        parent.setSelected(allSelected);
        TreeNode.BaseNodeViewHolder holder = parent.getViewHolder();
        if (holder instanceof CategoryViewHolder) {
            ((CategoryViewHolder) holder).updateCheckbox(allSelected);
        }

        updateParentSelection(parent.getParent());
    }
}
