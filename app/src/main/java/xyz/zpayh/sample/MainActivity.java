package xyz.zpayh.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import xyz.zpayh.foldinglayout.FoldingLayout;

public class MainActivity extends AppCompatActivity {

    private boolean mSkipAnimation  = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*final FoldingLayout foldingLayout = (FoldingLayout) findViewById(R.id.fl_root);
        foldingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foldingLayout.toggle(mSkipAnimation);
            }
        });
        
        findViewById(R.id.bt_fold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foldingLayout.toggle(mSkipAnimation);
            }
        });
        
        findViewById(R.id.bt_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "哈哈哈哈", Toast.LENGTH_SHORT).show();
            }
        });*/

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ListActivity.class));
            }
        });


        final FoldingLayout foldLayout = (FoldingLayout) findViewById(R.id.foldLayout);
        foldLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foldLayout.toggle(mSkipAnimation);
            }
        });

        findViewById(R.id.content_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foldLayout.toggle(mSkipAnimation);
            }
        });

        /*findViewById(R.id.bt_fold1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foldLayout.toggle(mSkipAnimation);
            }
        });

        findViewById(R.id.bt_toast1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "哈哈哈哈", Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
