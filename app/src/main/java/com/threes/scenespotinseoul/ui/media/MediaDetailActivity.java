package com.threes.scenespotinseoul.ui.media;

import static com.threes.scenespotinseoul.utilities.AppExecutorsHelperKt.runOnDiskIO;
import static com.threes.scenespotinseoul.utilities.AppExecutorsHelperKt.runOnMain;
import static com.threes.scenespotinseoul.utilities.ConstantsKt.EXTRA_MEDIA_ID;
import static com.threes.scenespotinseoul.utilities.ItemOffsetDecorationKt.DIR_RIGHT;
import static com.threes.scenespotinseoul.utilities.ItemOffsetDecorationKt.OFFSET_NORMAL;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.threes.scenespotinseoul.R;
import com.threes.scenespotinseoul.data.AppDatabase;
import com.threes.scenespotinseoul.data.model.Location;
import com.threes.scenespotinseoul.data.model.Media;
import com.threes.scenespotinseoul.data.model.MediaTag;
import com.threes.scenespotinseoul.data.model.Scene;
import com.threes.scenespotinseoul.data.model.Tag;
import com.threes.scenespotinseoul.utilities.ItemOffsetDecoration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MediaDetailActivity extends AppCompatActivity {

  private String media_id;

  private ImageView mMedia_image;
  private TextView mMedia_hash_tag;
  private TextView mMedia_title;
  private TextView mMedia_detail;
  private TextView mMedia_simpleText;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_details);

    mMedia_image = findViewById(R.id.iv_hero);
    mMedia_hash_tag = findViewById(R.id.tv_tag);
    mMedia_title = findViewById(R.id.tv_title);
    mMedia_detail = findViewById(R.id.tv_description);
    mMedia_simpleText = findViewById(R.id.tv_simple);

    TextView tvCategory1 = findViewById(R.id.tv_category1);
    TextView tvCategory2 = findViewById(R.id.tv_category2);
    ImageButton btnNavigateUp = findViewById(R.id.btn_navigate_up);
    RecyclerView recyclerView_scene = findViewById(R.id.rv_category1);
    RecyclerView recyclerView_location = findViewById(R.id.rv_category2);

    tvCategory1.setText(R.string.category_media_scenes);
    tvCategory2.setText(R.string.category_media_locations);

    btnNavigateUp.setOnClickListener(view -> finish());

    // 해당 미디어 명장면 리사이클러뷰 처리
    recyclerView_scene.addItemDecoration(new ItemOffsetDecoration(DIR_RIGHT, OFFSET_NORMAL));
    recyclerView_location.addItemDecoration(new ItemOffsetDecoration(DIR_RIGHT, OFFSET_NORMAL));

    mMedia_simpleText.setVisibility(View.GONE);

    Intent intent = getIntent();
    if (intent != null && intent.hasExtra(EXTRA_MEDIA_ID)) {
      media_id = intent.getStringExtra(EXTRA_MEDIA_ID);
    }

    // 해시태그 헬퍼 시도해봄
    //        mHashTagHelper =
    // HashTagHelper.Creator.create(getResources().getColor(R.color.colorPrimary), new
    // HashTagHelper.OnHashTagClickListener() {
    //            @Override
    //            public void onHashTagClicked(String hashTag) {
    //                Toast.makeText(getApplicationContext(), hashTag, Toast.LENGTH_SHORT).show();
    //            }
    //        });

    // 미디어 데이터 가져옴 Executor 사용
    AppDatabase db = AppDatabase.getInstance(this);
    runOnDiskIO(
        () -> {
          // List<Media> media = db.mediaDao().loadAll();
          Media mMedia = db.mediaDao().loadById(media_id);
          List<Scene> scenes = db.sceneDao().loadByMediaId(media_id);
          List<MediaTag> mediaTags = db.mediaTagDao().loadByMediaId(mMedia.getUuid());
          HashSet<Location> locations = new HashSet<>();
          for (Scene _scene : scenes){
              locations.add(db.locationDao().loadById(_scene.getLocationId()));
          }
          List<Tag> tags = new ArrayList<>();
          for (int i = 0; i < mediaTags.size(); i++) {
            tags.add(db.tagDao().loadById(mediaTags.get(i).getTagId()));
            Log.e("태그", tags.get(i).getName());
          }

          runOnMain(
              () -> {
                MediaSceneAdapter adapter_scene = new MediaSceneAdapter(scenes);
                recyclerView_scene.setAdapter(adapter_scene);
                recyclerView_scene.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                recyclerView_scene.setHasFixedSize(true);
                MediaLocationAdapter adapter = new MediaLocationAdapter(locations);
                recyclerView_location.setAdapter(adapter);
                recyclerView_location.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                recyclerView_location.setHasFixedSize(true);

                // 미디어 대표 이미지 세팅
                RequestOptions requestOptions =
                    new RequestOptions().placeholder(android.R.color.darker_gray).centerCrop();

                Glide.with(this)
                    .asBitmap()
                    .load(mMedia.getImage())
                    .apply(requestOptions)
                    .listener(new RequestListener<Bitmap>() {
                      @Override
                      public boolean onLoadFailed(@Nullable GlideException e, Object model,
                          Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                      }

                      @Override
                      public boolean onResourceReady(Bitmap resource, Object model,
                          Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (resource != null) {
                          Palette.from(resource).generate(palette -> {
                            if (palette != null) {
                                getWindow().setStatusBarColor(palette.getDarkVibrantColor(
                                    ContextCompat.getColor(MediaDetailActivity.this, R.color.colorPrimaryDark)));
                            }
                          });
                        }
                        return false;
                      }
                    })
                    .into(mMedia_image);

                // 미디어 타이틀 세팅
                mMedia_title.setText(mMedia.getName());

                // 미디어 상세설명 세팅
                mMedia_detail.setText(mMedia.getDesc());

                if (mMedia_detail.getLineCount() > 2) {
                  int tLineCount = mMedia_detail.getLineCount();
                  mMedia_detail.setMaxLines(2);
                  mMedia_detail.setEllipsize(TextUtils.TruncateAt.END);

                  mMedia_detail.setOnClickListener(
                      v -> {
                        mMedia_detail.setMaxLines(tLineCount);
                        mMedia_simpleText.setVisibility(View.VISIBLE);
                      });

                  // 미디어 간략히보기 이벤트
                  mMedia_simpleText.setOnClickListener(
                      v -> {
                        mMedia_detail.setMaxLines(2);
                        mMedia_simpleText.setVisibility(View.GONE);
                      });
                }

                // 미디어 해시티그 세팅
                StringBuilder mTag = new StringBuilder();
                for (Tag tag : tags) {
                  mTag.append("#").append(tag.getName()).append(" ");
                }
                mMedia_hash_tag.setText(mTag.toString());
              });
        });
  }
}
