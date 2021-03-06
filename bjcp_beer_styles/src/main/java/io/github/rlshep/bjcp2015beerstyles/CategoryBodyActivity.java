package io.github.rlshep.bjcp2015beerstyles;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.github.rlshep.bjcp2015beerstyles.controllers.BjcpController;
import io.github.rlshep.bjcp2015beerstyles.db.BjcpDataHelper;
import io.github.rlshep.bjcp2015beerstyles.domain.Category;
import io.github.rlshep.bjcp2015beerstyles.domain.Section;
import io.github.rlshep.bjcp2015beerstyles.domain.VitalStatistics;
import io.github.rlshep.bjcp2015beerstyles.exceptions.ExceptionHandler;
import io.github.rlshep.bjcp2015beerstyles.formatters.StringFormatter;
import io.github.rlshep.bjcp2015beerstyles.listeners.GestureListener;


public class CategoryBodyActivity extends BjcpActivity {
    private static final String VITAL_HEADER = "Vital Statistics";
    private static final String SRM_PREFIX = "srm_";
    private GestureDetector gestureDetector;
    private String categoryId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_category_body);
        Bundle extras = getIntent().getExtras();
        String searchedText = "";

        if (extras != null) {
            String title = extras.getString("CATEGORY") + " - " + extras.getString("CATEGORY_NAME");
            setupToolbar(R.id.scbToolbar, title, false, true);
            categoryId = extras.getString("CATEGORY_ID");
            searchedText = extras.getString("SEARCHED_TEXT");
        }

        setBody(searchedText);
        gestureDetector = new GestureDetector(this, new GestureListener());
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        boolean eventReturn;
        boolean eventConsumed = gestureDetector.onTouchEvent(event);

        if (eventConsumed) {
            if (GestureListener.SWIPE_LEFT.equals(GestureListener.currentGesture)) {
                changeCategory(-1);
            } else if (GestureListener.SWIPE_RIGHT.equals(GestureListener.currentGesture)) {
                changeCategory(1);
            }

            eventReturn = true;
        } else {
            eventReturn = super.dispatchTouchEvent(event);
        }

        return eventReturn;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Category category = BjcpDataHelper.getInstance(this).getCategory(categoryId);
                BjcpController.loadCategoryList(this, BjcpDataHelper.getInstance(this).getCategory(((Long) category.getParentId()).toString()));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setBody(String searchedText) {
        List<VitalStatistics> vitalStatisticses = BjcpDataHelper.getInstance(this).getVitalStatistics(categoryId);
        setMainText(searchedText, vitalStatisticses);
        setSrms(vitalStatisticses);
    }

    private void setSrms(List<VitalStatistics> vitalStatisticses) {
        int i = 1;

        for (VitalStatistics vitalStatistics : vitalStatisticses) {
            if (null != vitalStatistics.getSrmStart()) {
                setSrm(vitalStatistics, i);
                i++;
            }
        }
    }

    private void setMainText(String searchedText, List<VitalStatistics> vitalStatisticses) {
        String text = getSectionsBody(categoryId) + getVitalStatistics(vitalStatisticses);

        TextView sectionsTextView = (TextView) findViewById(R.id.sectionsText);
        sectionsTextView.setText(Html.fromHtml(StringFormatter.getHighlightedText(text, searchedText)));
    }

    private String getSectionsBody(String categoryId) {
        String body = "";
        for (Section section : BjcpDataHelper.getInstance(this).getCategorySections(categoryId)) {
            body += "<big><b> " + section.getHeader() + "</b></big><br>";
            body += section.getBody() + "<br><br>";
        }

        return body;
    }

    private void setSrm(VitalStatistics vitalStatistics, int i) {
        getSrmImageView("imgSrmBegin", i).setImageResource(getResources().getIdentifier(SRM_PREFIX + getStartSrm(vitalStatistics), "drawable", getPackageName()));
        getSrmImageView("imgSrmBegin", i).setContentDescription(getString(R.string.beginSrm) + vitalStatistics.getSrmStart()); // For disabled accessibility.
        getSrmImageView("imgSrmEnd", i).setImageResource(getResources().getIdentifier(SRM_PREFIX + getEndSrm(vitalStatistics), "drawable", getPackageName()));
        getSrmImageView("imgSrmEnd", i).setContentDescription(getString(R.string.endSrm) + vitalStatistics.getSrmEnd()); // For disabled accessibility.

        getSrmTextView("srmText", i).setText(Html.fromHtml("<br><b>" + vitalStatistics.getHeader() + " SRM:</b>"));

        getSrmImageView("imgSrmBegin", i).setVisibility(View.VISIBLE);
        getSrmImageView("imgSrmEnd", i).setVisibility(View.VISIBLE);
        getSrmTextView("srmText", i).setVisibility(View.VISIBLE);
    }

    private String getVitalStatistics(List<VitalStatistics> vitalStatisticses) {
        String vitals = "";

        if (!vitalStatisticses.isEmpty()) {
            vitals += "<big><b> " + VITAL_HEADER + "</b></big>";
        }

        for (VitalStatistics vitalStatistics : vitalStatisticses) {
            if (null != vitalStatistics.getIbuStart()) {
                vitals += "<br><b>" + vitalStatistics.getHeader() + " IBUs:</b> " + vitalStatistics.getIbuStart() + " - " + vitalStatistics.getIbuEnd();
            }
            if (null != vitalStatistics.getOgStart()) {
                vitals += "<br><b>" + vitalStatistics.getHeader() + " OG:</b> " + vitalStatistics.getOgStart() + " - " + vitalStatistics.getOgEnd();
            }
            if (null != vitalStatistics.getAbvStart()) {
                vitals += "<br><b>" + vitalStatistics.getHeader() + " ABV:</b> " + vitalStatistics.getAbvStart() + " - " + vitalStatistics.getAbvEnd();
            }
            if (null != vitalStatistics.getFgStart()) {
                vitals += "<br><b>" + vitalStatistics.getHeader() + " FG:</b> " + vitalStatistics.getFgStart() + " - " + vitalStatistics.getFgEnd();
            }
        }

        return vitals;
    }

    private ImageView getSrmImageView(String srm, int i) {
        int id = getResources().getIdentifier(srm + i, "id", getPackageName());
        return (ImageView) findViewById(id);
    }

    private TextView getSrmTextView(String srm, int i) {
        int id = getResources().getIdentifier(srm + i, "id", getPackageName());
        return (TextView) findViewById(id);
    }

    private void changeCategory(int i) {
        Category category = BjcpDataHelper.getInstance(this).getCategory(categoryId);
        List<Category> subCategories = BjcpDataHelper.getInstance(this).getCategoriesByParent(category.getParentId());
        int newOrder = category.getOrderNumber() + i;

        for (Category sc : subCategories) {
            if (newOrder == sc.getOrderNumber()) {
                BjcpController.loadCategoryBody(this, sc);
            }
        }
    }

    private String getStartSrm(VitalStatistics vitalStatistics) {
        double floor = Math.floor(Double.parseDouble(vitalStatistics.getSrmStart()));
        return ((Integer) Double.valueOf(floor).intValue()).toString();
    }

    private String getEndSrm(VitalStatistics vitalStatistics) {
        double ceil = Math.ceil(Double.parseDouble(vitalStatistics.getSrmEnd()));
        return ((Integer) Double.valueOf(ceil).intValue()).toString();
    }
}