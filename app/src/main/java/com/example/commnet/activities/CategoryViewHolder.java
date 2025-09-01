package com.example.commnet.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.commnet.R;
import com.unnamed.b.atv.model.TreeNode;

public class CategoryViewHolder extends TreeNode.BaseNodeViewHolder<Node> {
    private CheckBox checkBox;

    public CategoryViewHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, Node value) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.tree_node_category, null, false);

        TextView textView = view.findViewById(R.id.categoryText);
        checkBox = view.findViewById(R.id.category_checkbox);

        textView.setText("▶ " + value.name);

        int depth = node.getLevel();
        int paddingLeft = depth * 50;
        view.setPadding(paddingLeft, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());

        checkBox.setChecked(node.isSelected());

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            node.setSelected(isChecked);
            setChildrenSelection(node, isChecked);
            updateParentSelection(node.getParent());
        });

        return view;
    }

    public void updateCheckbox(boolean isChecked) {
        if (checkBox != null) checkBox.setChecked(isChecked);
    }

    private void setChildrenSelection(TreeNode node, boolean isChecked) {
        for (TreeNode child : node.getChildren()) {
            child.setSelected(isChecked);
            TreeNode.BaseNodeViewHolder holder = child.getViewHolder();

            if (holder instanceof UserViewHolder) {
                ((UserViewHolder) holder).updateCheckbox(isChecked);
            } else if (holder instanceof CategoryViewHolder) {
                ((CategoryViewHolder) holder).updateCheckbox(isChecked);
            }

            setChildrenSelection(child, isChecked);
        }
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
