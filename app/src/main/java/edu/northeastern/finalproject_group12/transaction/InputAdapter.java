package edu.northeastern.finalproject_group12.transaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.finalproject_group12.R;

public class InputAdapter extends RecyclerView.Adapter<InputAdapter.InputItemHolder> {

    private final List<View> inputViews;

    public InputAdapter(List<View> inputViews) {

        this.inputViews = inputViews;
    }

    @NonNull
    @Override
    public InputItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new InputItemHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.constraintlayout_component, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull InputItemHolder holder, int position) {

        holder.bindView(inputViews.get(position));
    }

    @Override
    public int getItemCount() {

        return inputViews.size();
    }

    protected static class InputItemHolder extends RecyclerView.ViewHolder {

        private View constraintLayout1;
        public InputItemHolder(View constraintLayout) {

            super(constraintLayout);
            this.constraintLayout1 = constraintLayout;
        }

        public void bindView(View view) {

            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }

            ConstraintLayout constraintLayout = (ConstraintLayout) constraintLayout1.findViewById(R.id.constraint_layout);
            constraintLayout.addView(view);
        }

    }

    public void advancedFeature(boolean isAdvancedClicked, View v1, View v2) {

        if (isAdvancedClicked) {

            inputViews.remove(getItemCount() - 2);
            notifyItemRemoved(getItemCount() - 1);

            inputViews.remove(getItemCount() - 2);
            notifyItemRemoved(getItemCount() - 1);
        }
        else {

            inputViews.add(getItemCount() - 1, v1);
            notifyItemInserted(getItemCount() - 2);

            inputViews.add(getItemCount() - 1, v2);
            notifyItemInserted(getItemCount() - 2);
        }

    }

}
