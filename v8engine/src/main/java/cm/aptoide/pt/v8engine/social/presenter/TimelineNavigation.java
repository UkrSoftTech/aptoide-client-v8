package cm.aptoide.pt.v8engine.social.presenter;

import cm.aptoide.pt.v8engine.view.app.AppViewFragment;

/**
 * Created by jdandrade on 30/06/2017.
 */

public interface TimelineNavigation {

  void navigateToAppView(long appId, String packageName, AppViewFragment.OpenType openType);

  void navigateToAppView(String packageName, AppViewFragment.OpenType openType);

  void navigateToStore(String storeName, String storeTheme);
}
