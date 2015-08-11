package io.github.rlshep.bjcp2015beerstyles;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.rlshep.bjcp2015beerstyles.adapters.CategoriesListAdapter;
import io.github.rlshep.bjcp2015beerstyles.db.BjcpDataHelper;
import io.github.rlshep.bjcp2015beerstyles.domain.SubCategory;


public class SubCategoryListActivity extends AppCompatActivity {
    private BjcpDataHelper dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String categoryId = "";

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category_list);
        dbHandler = BjcpDataHelper.getInstance(this);

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            String title = extras.getString("CATEGORY") + " - " + extras.getString("CATEGORY_NAME");
            Toolbar toolbar = (Toolbar) findViewById(R.id.sclToolbar);
            toolbar.setTitle(title);

            categoryId = extras.getString("CATEGORY_ID");
        }

        setListView(categoryId);
    }

    private void setListView(String categoryId) {
        List listView = new ArrayList();

        listView.addAll(dbHandler.getCategorySections(categoryId));
        listView.addAll(dbHandler.getSubCategories(categoryId));

        ListAdapter subCategoryAdapter = new CategoriesListAdapter(this, listView);
        ListView subCategoryListView = (ListView) findViewById(R.id.subCategoryListView);
        subCategoryListView.setAdapter(subCategoryAdapter);

        subCategoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position) instanceof SubCategory) {
                    SubCategory subCategory = (SubCategory) parent.getItemAtPosition(position);
                    loadSubCategoryBody(subCategory);
                }
            }
        });

        subCategoryListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                boolean consumed = false;

                if (parent.getItemAtPosition(position) instanceof SubCategory) {
                    addSubCategoryToOnTap((SubCategory) parent.getItemAtPosition(position));
                    consumed = true;
                }

                return consumed;
            }
        });
    }

    private void loadSubCategoryBody(SubCategory subCategory) {
        Intent i = new Intent(this, SubCategoryBodyActivity.class);

        i.putExtra("CATEGORY_ID", (new Long(subCategory.get_categoryId())).toString());
        i.putExtra("SUB_CATEGORY_ID", (new Long(subCategory.get_id())).toString());
        i.putExtra("SUB_CATEGORY", subCategory.get_subCategory());
        i.putExtra("SUB_CATEGORY_NAME", subCategory.get_name());
        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivity(i);
    }

    private void addSubCategoryToOnTap(SubCategory subCategory) {
        subCategory.set_tapped(true);

        dbHandler.updateSubCategoryUntapped(subCategory);

        Toast.makeText(getApplicationContext(), R.string.on_tap_success, Toast.LENGTH_SHORT).show();
    }
}
