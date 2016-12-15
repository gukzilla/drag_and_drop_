package ru.gukzilla.testjob;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import java.util.List;

import ru.gukzilla.testjob.models.Note;

public class MainActivity extends AppCompatActivity {

    private DataBase dataBase;
    private int count = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_minimal);

        dataBase = new DataBase(this);
        dataBase.open();

        if(dataBase.isCreated()) {
            Note firstNote = new Note(dataBase.generateId());
            count = 1;
            notesGeneration(firstNote, new NoteCallback() {
                @Override
                public boolean onComplete(Note note) {
                    note.setName(Integer.toString(count));
                    dataBase.addNote(note);
                    return ++count <= 100;
                }
            });
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // Setup D&D feature and RecyclerView
        RecyclerViewDragDropManager dragMgr = new RecyclerViewDragDropManager();
        dragMgr.setInitiateOnMove(false);
        dragMgr.setInitiateOnLongPress(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(dragMgr.createWrappedAdapter(new MyAdapter()));
        dragMgr.attachRecyclerView(recyclerView);
    }

    private interface NoteCallback {
        boolean onComplete(Note note);
    }

    private void notesGeneration(Note note, NoteCallback noteCallback) {
        Note next = new Note(dataBase.generateId());

        note.setNext(next.get_id());
        next.setPrev(note.get_id());

        if(noteCallback.onComplete(note)) {
            notesGeneration(next, noteCallback);
        }
    }


    @Override
    public void onDestroy() {
        dataBase.close();
        super.onDestroy();
    }



    private static class MyViewHolder extends AbstractDraggableItemViewHolder {
        TextView textView;

        public MyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> implements DraggableItemAdapter<MyViewHolder> {
        List<Note> mItems;

        public MyAdapter() {
            setHasStableIds(true); // this is required for D&D feature.

            mItems = dataBase.getAllNotes();
        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).get_id().hashCode(); // need to return stable (= not change even after reordered) value
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_minimal, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Note item = mItems.get(position);
            holder.textView.setText(item.getName());
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public void onMoveItem(int fromPosition, int toPosition) {

            {
                Note prev = getLeftOfPosition(fromPosition);
                Note next = getRightIdOfPosition(fromPosition);
                relinkNotes(prev, next);
                dataBase.updateNotes(prev, next);
            }

            {
                Note fromNote = mItems.get(fromPosition);
                Note prev = getLeftOfPosition(toPosition);
                Note next = getRightIdOfPosition(toPosition);

                relinkNotes(prev, fromNote);
                relinkNotes(fromNote, next);

                dataBase.updateNotes(prev, fromNote, next);
            }

            mItems.add(toPosition, mItems.remove(fromPosition));
            notifyItemMoved(fromPosition, toPosition);
        }

        private Note getLeftOfPosition(int position) {
            position -= 1; // left
            if(position < 0) {
                return null;
            }
            return mItems.get(position);
        }

        private Note getRightIdOfPosition(int position) {
            if(position > mItems.size() - 1) {
                return null;
            }
            return mItems.get(position);
        }

        private void relinkNotes(Note left, Note right) {
            String leftId = left != null ? left.get_id() : "";
            String rightId = right != null ? right.get_id() : "";

            if(left != null) {
                left.setNext(rightId);
            }

            if(right != null) {
                right.setPrev(leftId);
            }
        }

        @Override
        public boolean onCheckCanStartDrag(MyViewHolder holder, int position, int x, int y) {
            return true;
        }

        @Override
        public ItemDraggableRange onGetItemDraggableRange(MyViewHolder holder, int position) {
            return null;
        }

        @Override
        public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
            return true;
        }
    }
}