package xyz.zpayh.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import xyz.zpayh.adapter.BaseMultiAdapter;
import xyz.zpayh.adapter.BaseViewHolder;
import xyz.zpayh.adapter.DefaultMultiItem;
import xyz.zpayh.adapter.OnItemClickListener;
import xyz.zpayh.foldinglayout.FoldingLayout;

public class ListActivity extends AppCompatActivity {

    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        RecyclerView list = (RecyclerView) findViewById(R.id.rv_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MyAdapter();
        list.setAdapter(mAdapter);


        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull View view, int adapterPosition) {
                if (view.getId() == R.id.fl_root){
                    FoldingLayout foldLayout = (FoldingLayout) view;
                    Data data = (Data) mAdapter.getData(adapterPosition);
                    if (data != null){
                        //改变
                        data.setKeepState(foldLayout.isFolded()?UN_FOLD:FOLD);
                    }
                    foldLayout.toggle(false);
                }
            }
        });

        List<Data> data = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            data.add(new Data());
        }
        mAdapter.setData(data);
    }

    private class MyAdapter extends BaseMultiAdapter{

        @Override
        public void bind(BaseViewHolder holder, int layoutRes) {
            holder.setClickable(R.id.fl_root,true);
        }
    }

    public static final int FOLD = 0;
    public static final int UN_FOLD = 1;
    private class Data extends DefaultMultiItem<String>{

        public int mKeepState;

        public Data() {
            super(R.layout.item_list);
            mKeepState = FOLD;
        }

        public void setKeepState(int keepState) {
            mKeepState = keepState;
        }

        @Override
        public void convert(BaseViewHolder holder) {
            FoldingLayout layout = holder.find(R.id.fl_root);
            if (layout != null){
                if (mKeepState == FOLD) {
                    layout.fold(true);
                }else {
                    layout.unfold(true);
                }
            }
        }
    }
}
