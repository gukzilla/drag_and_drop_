package ru.gukzilla.testjob;

import android.app.ProgressDialog;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;

import java.util.ArrayList;
import java.util.List;

import ru.gukzilla.testjob.models.Note;

public class MainActivity extends AppCompatActivity {

    private DataBase dataBase;
    private final String GEN_TAG = "GENERATION";
    private final String TAG = getClass().getSimpleName();
    private final int numberOfGeneration = 1000;

    interface GenerationCallback {
        void onComplete();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_minimal);

        dataBase = new DataBase(this);
        dataBase.open();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }, 300);
    }

    private void loadData() {
        if(dataBase.isCreated()) {
            generationAsync(new GenerationCallback() {
                @Override
                public void onComplete() {
                    recreateRecyclerView();
                }
            });
        } else {
            recreateRecyclerView();
        }
    }

    private void recreateRecyclerView() {
        final View progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        dataBase.getAllNotesAsync(new DataBase.NotesListener() {
            @Override
            public void onResult(List<Note> notes) {
                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
                // Setup D&D feature and RecyclerView
                RecyclerViewDragDropManager dragMgr = new RecyclerViewDragDropManager();
                dragMgr.setInitiateOnMove(false);
                dragMgr.setInitiateOnLongPress(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(dragMgr.createWrappedAdapter(new RecyclerAdapter(notes)));
                dragMgr.attachRecyclerView(recyclerView);

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void generationSync(DataBase.ProgressListener generationCallback, DataBase.ProgressListener insertCallback) {
        long time = System.currentTimeMillis();
        Log.i(GEN_TAG, "start time = " + time);

        List<Note> notes = new ArrayList<>();
        generationCallback.preStart(numberOfGeneration);

        Note left = null;
        for(int count = 1; count <= numberOfGeneration; count ++) {
            Note right = new Note(dataBase.generateId());
            right.setName(Integer.toString(count));
            relinkNotes(left, right);
            notes.add(right);

            left = right;

            generationCallback.onResult(count);
        }
        generationCallback.onComplete();

        Log.i(GEN_TAG, "time of generation = " + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        dataBase.addNotes(notes, insertCallback);
        Log.i(GEN_TAG, "time of addition = " + (System.currentTimeMillis() - time));
    }

    private void generationAsync(final GenerationCallback generationCallback) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Object generation...");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        pd.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                generationSync(new DataBase.ProgressListener() {
                    @Override
                    public void preStart(int size) {
                        pd.setIndeterminate(false);
                        pd.setMax(size);
                    }

                    @Override
                    public void onResult(int progress) {
                        pd.setProgress(progress);
                    }

                    @Override
                    public void onComplete() {

                    }
                }, new DataBase.ProgressListener() {
                    @Override
                    public void preStart(int size) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.setMessage("Creating objects in the database...");
                            }
                        });
                        pd.setMax(size);
                    }

                    @Override
                    public void onResult(int progress) {
                        pd.setProgress(progress);
                    }

                    @Override
                    public void onComplete() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.dismiss();
                                generationCallback.onComplete();
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private void relinkNotes(Note left, Note right) {
        String leftId = left != null ? left.get_id() : null;
        String rightId = right != null ? right.get_id() : null;

        if(left != null) {
            left.setNext(rightId);
        }

        if(right != null) {
            right.setPrev(leftId);
        }
    }

    @Override
    public void onDestroy() {
        dataBase.close();
        super.onDestroy();
    }


    private class RecyclerAdapter extends RecyclerView.Adapter<MyViewHolder> implements DraggableItemAdapter<MyViewHolder> {
        List<Note> mItems;

        public RecyclerAdapter(List<Note> mItems) {
            setHasStableIds(true); // this is required for D&D feature.
            this.mItems = mItems;
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

            Note prev1 = getNote(fromPosition - 1);
            Note next1 = getNote(fromPosition + 1);
            relinkNotes(prev1, next1);
            dataBase.updateNotes(prev1, next1);

            Note fromNote = mItems.remove(fromPosition);
            Note prev2 = getNote(toPosition - 1);
            Note next2 = getNote(toPosition);
            relinkNotes(prev2, fromNote);
            relinkNotes(fromNote, next2);
            dataBase.updateNotes(prev2, fromNote, next2);

            mItems.add(toPosition, fromNote);
            notifyItemMoved(fromPosition, toPosition);
        }

        private Note getNote(int position) {
            if(position < 0 || position > mItems.size() - 1) {
                return null;
            }

            return mItems.get(position);
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