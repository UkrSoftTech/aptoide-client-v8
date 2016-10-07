/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 16/08/2016.
 */

package cm.aptoide.pt.v8engine.view.recycler.widget.implementations.grid;

import android.content.DialogInterface;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import cm.aptoide.pt.actions.PermissionRequest;
import cm.aptoide.pt.database.accessors.AccessorFactory;
import cm.aptoide.pt.database.accessors.DeprecatedDatabase;
import cm.aptoide.pt.database.accessors.InstalledAccessor;
import cm.aptoide.pt.database.realm.Installed;
import cm.aptoide.pt.imageloader.ImageLoader;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.fragment.implementations.AppViewFragment;
import cm.aptoide.pt.v8engine.util.FragmentUtils;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid.UpdateDisplayable;
import cm.aptoide.pt.v8engine.view.recycler.widget.Displayables;
import cm.aptoide.pt.v8engine.view.recycler.widget.Widget;
import com.jakewharton.rxbinding.view.RxView;
import io.realm.Realm;
import lombok.Cleanup;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by neuro on 17-05-2016.
 */
@Displayables({ UpdateDisplayable.class }) public class UpdateWidget
    extends Widget<UpdateDisplayable> {

  private View updateRowRelativeLayout;
  private TextView labelTextView;
  private ImageView iconImageView;
  private ImageView imgUpdateLayout;
  private TextView installedVernameTextView;
  private TextView updateVernameTextView;
  private TextView textUpdateLayout;
  private ViewGroup updateButtonLayout;
  private UpdateDisplayable displayable;
  private LinearLayout updateLayout;
  private ProgressBar progressBar;
  private CompositeSubscription subscriptions;

  public UpdateWidget(View itemView) {
    super(itemView);
  }

  @Override protected void assignViews(View itemView) {
    updateRowRelativeLayout = itemView.findViewById(R.id.updateRowRelativeLayout);
    labelTextView = (TextView) itemView.findViewById(R.id.name);
    iconImageView = (ImageView) itemView.findViewById(R.id.icon);
    installedVernameTextView = (TextView) itemView.findViewById(R.id.app_installed_version);
    updateVernameTextView = (TextView) itemView.findViewById(R.id.app_update_version);
    updateButtonLayout = (ViewGroup) itemView.findViewById(R.id.updateButtonLayout);
    updateLayout = (LinearLayout) itemView.findViewById(R.id.update_layout);
    imgUpdateLayout = (ImageView) itemView.findViewById(R.id.img_update_layout);
    textUpdateLayout = (TextView) itemView.findViewById(R.id.text_update_layout);
    progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
  }

  @Override public void bindView(UpdateDisplayable updateDisplayable) {
    this.displayable = updateDisplayable;
    final String packageName = updateDisplayable.getPackageName();
    final InstalledAccessor accessor = AccessorFactory.getAccessorFor(Installed.class);

    if (subscriptions == null || subscriptions.isUnsubscribed()) {
      subscriptions = new CompositeSubscription();
    }

    subscriptions.add(accessor.get(packageName)
        .first()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(installed -> installedVernameTextView.setText(installed.getVersionName()),
            throwable -> throwable.printStackTrace()));
    labelTextView.setText(updateDisplayable.getLabel());
    updateVernameTextView.setText(updateDisplayable.getUpdateVersionName());
    ImageLoader.load(updateDisplayable.getIcon(), iconImageView);

    updateRowRelativeLayout.setOnClickListener(v -> FragmentUtils.replaceFragmentV4(getContext(),
        AppViewFragment.newInstance(updateDisplayable.getAppId())));

    final View.OnLongClickListener longClickListener = v -> {
      AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
      builder.setTitle(R.string.ignore_update)
          .setCancelable(true)
          .setNegativeButton(R.string.no, null)
          .setPositiveButton(R.string.yes, (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
              @Cleanup Realm realm1 = DeprecatedDatabase.get();
              DeprecatedDatabase.UpdatesQ.setExcluded(packageName, true, realm1);
            }
            dialog.dismiss();
          });

      builder.create().show();

      return true;
    };

    updateRowRelativeLayout.setOnLongClickListener(longClickListener);
  }

  @Override public void onViewAttached() {
    subscriptions.add(RxView.clicks(updateButtonLayout)
        .flatMap(
            click -> displayable.downloadAndInstall(getContext(), (PermissionRequest) getContext()))
        .retry()
        .subscribe(o -> {
        }, throwable -> throwable.printStackTrace()));

    subscriptions.add(displayable.getUpdates()
        .filter(downloadProgress -> downloadProgress.getRequest().getAppId()
            == displayable.getDownload().getAppId())
        .map(downloadProgress -> displayable.isDownloadingOrInstalling(downloadProgress))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(shouldShow -> showProgress(shouldShow),
            throwable -> throwable.printStackTrace()));
  }

  @Override public void onViewDetached() {
    if (subscriptions != null && !subscriptions.isUnsubscribed()) {
      subscriptions.unsubscribe();
    }
  }

  @UiThread private void showProgress(Boolean showProgress) {
    if (showProgress) {
      textUpdateLayout.setVisibility(View.GONE);
      imgUpdateLayout.setVisibility(View.GONE);
      progressBar.setVisibility(View.VISIBLE);
    } else {
      textUpdateLayout.setVisibility(View.VISIBLE);
      imgUpdateLayout.setVisibility(View.VISIBLE);
      progressBar.setVisibility(View.GONE);
    }
  }
}
