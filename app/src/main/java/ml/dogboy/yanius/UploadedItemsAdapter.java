package ml.dogboy.yanius;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ml.dogboy.yanius.upload.UploadedItem;

public class UploadedItemsAdapter extends ArrayAdapter<UploadedItem> {

    private final List<UploadedItem> items;

    public UploadedItemsAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<UploadedItem> objects) {
        super(context, resource, objects);
        this.items = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.uploaded_item, null);
        }
        UploadedItem item = this.items.get(this.items.size() - 1 - position);
        if (item != null) {
            TextView fileName = (TextView) view.findViewById(R.id.uploaded_item_filename);
            TextView fileSize = (TextView) view.findViewById(R.id.uploaded_item_size);
            fileName.setText(item.getFileName() + (item.isUploading()
                            ? " (" + this.getContext().getResources().getText(R.string.uploading) + ")"
                            : (!item.isSucceeded() ? " (" + this.getContext().getResources().getText(R.string.errored) + ")" : "")));
            fileSize.setText(String.valueOf(Formatter.formatFileSize(this.getContext(), item.getFileSize())));
        }
        return view;
    }

}
