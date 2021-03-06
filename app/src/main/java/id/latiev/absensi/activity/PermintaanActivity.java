package id.latiev.absensi.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import id.latiev.absensi.R;
import id.latiev.absensi.helper.CustomListAdapter;
import id.latiev.absensi.model.Kegiatan;
import id.latiev.absensi.network.AppController;

public class PermintaanActivity extends AppCompatActivity {

    // set adapter
    private CustomListAdapter adapter;

    // set display
    private RecyclerView recyclerView;

    // set variabel
    private List<Kegiatan> kegiatanList;
    private String urlServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permintaan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_permintaan);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        initCollapsingToolbar();
        urlServer = LoginActivity.preferencesURLServer.getString(LoginActivity.KEY_URL, "");

        recyclerView = (RecyclerView) findViewById(R.id.rv_permintaan);
        kegiatanList = new ArrayList<>();

        adapter = new CustomListAdapter(PermintaanActivity.this, kegiatanList);
        getPermintaan(MainActivity.preferencesAbsensi.getString(MainActivity.KEY_ID, ""));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(PermintaanActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.ctl_ap_collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.abl_ap_appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle("Daftar Permintaan");
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    private void getPermintaan(String idPresensi) {
        String url = "http://" + urlServer + "/api/datasources/permintaan_list_by_id_presensi?id_presensi=" + idPresensi;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray datas = response.getJSONArray("data");
                    if (datas.length() > 0) {
                        for (int i = 0; i < datas.length(); i++) {
                            JSONObject data = datas.getJSONObject(i);
                            kegiatanList.add(new Kegiatan(splitTime(data.getString("waktu")), data.getString("permintaan"), data.getString("keterangan")));
                        }
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("error catch ", e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error listener ", error.toString());
            }
        });
        AppController.getInstance().addToRequestQueue(request, "Batal");
    }

    private String splitTime(String time) {
        String[] parts = time.split(":");
        return parts[0] + ":" + parts[1];
    }
}
