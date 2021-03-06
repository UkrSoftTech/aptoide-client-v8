package cm.aptoide.pt.app.view;

import cm.aptoide.pt.crashreports.CrashReport;
import cm.aptoide.pt.dataprovider.model.v2.GetAdsResponse;
import cm.aptoide.pt.dataprovider.model.v7.Event;
import cm.aptoide.pt.home.AdBundle;
import cm.aptoide.pt.home.AdClick;
import cm.aptoide.pt.home.AdHomeEvent;
import cm.aptoide.pt.home.AdMapper;
import cm.aptoide.pt.home.AdsTagWrapper;
import cm.aptoide.pt.home.AppHomeEvent;
import cm.aptoide.pt.home.FakeBundleDataSource;
import cm.aptoide.pt.home.HomeAnalytics;
import cm.aptoide.pt.home.HomeBundle;
import cm.aptoide.pt.home.HomeBundlesModel;
import cm.aptoide.pt.home.HomeEvent;
import cm.aptoide.pt.home.HomeNavigator;
import cm.aptoide.pt.presenter.View;
import cm.aptoide.pt.view.BundleEvent;
import cm.aptoide.pt.view.app.Application;
import java.util.Collections;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rx.Single;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by D01 on 08/06/2018.
 */

public class MoreBundlePresenterTest {
  @Mock private MoreBundleFragment view;
  @Mock private CrashReport crashReporter;
  @Mock private HomeNavigator homeNavigator;
  @Mock private MoreBundleManager moreBundleManager;
  @Mock private HomeAnalytics homeAnalytics;

  private MoreBundlePresenter presenter;
  private HomeBundlesModel bundlesModel;
  private PublishSubject<View.LifecycleEvent> lifecycleEvent;
  private PublishSubject<AppHomeEvent> appClickEvent;
  private PublishSubject<AdHomeEvent> adClickEvent;
  private PublishSubject<HomeEvent> moreClickEvent;
  private PublishSubject<Object> bottomReachedEvent;
  private PublishSubject<Void> pullToRefreshEvent;
  private PublishSubject<Void> retryClickedEvent;
  private HomeBundle localTopAppsBundle;
  private Application aptoide;
  private PublishSubject<HomeEvent> bundleScrolledEvent;
  private BundleEvent bundleEvent;

  @Before public void setupHomePresenter() {
    MockitoAnnotations.initMocks(this);

    lifecycleEvent = PublishSubject.create();
    appClickEvent = PublishSubject.create();
    adClickEvent = PublishSubject.create();
    moreClickEvent = PublishSubject.create();
    bottomReachedEvent = PublishSubject.create();
    pullToRefreshEvent = PublishSubject.create();
    retryClickedEvent = PublishSubject.create();
    bundleScrolledEvent = PublishSubject.create();
    bundleEvent = new BundleEvent("title", "action");

    presenter =
        new MoreBundlePresenter(view, moreBundleManager, Schedulers.immediate(), crashReporter,
            homeNavigator, new AdMapper(), bundleEvent, homeAnalytics);
    aptoide =
        new Application("Aptoide", "http://via.placeholder.com/350x150", 0, 1000, "cm.aptoide.pt",
            300, "", false);
    FakeBundleDataSource fakeBundleDataSource = new FakeBundleDataSource();
    bundlesModel = new HomeBundlesModel(fakeBundleDataSource.getFakeBundles(), false, 0);
    localTopAppsBundle = bundlesModel.getList()
        .get(0);

    when(view.getLifecycleEvent()).thenReturn(lifecycleEvent);
    when(view.appClicked()).thenReturn(appClickEvent);
    when(view.adClicked()).thenReturn(adClickEvent);
    when(view.moreClicked()).thenReturn(moreClickEvent);
    when(view.reachesBottom()).thenReturn(bottomReachedEvent);
    when(view.refreshes()).thenReturn(pullToRefreshEvent);
    when(view.retryClicked()).thenReturn(retryClickedEvent);
    when(view.bundleScrolled()).thenReturn(bundleScrolledEvent);
  }

  @Test public void onCreateSetupToolbarTest() {
    //Given an initialised MoreBundlePresenter
    presenter.onCreateSetupToolbar();

    lifecycleEvent.onNext(View.LifecycleEvent.CREATE);
    //It should fill the toolbar with the correct info
    verify(view).setToolbarInfo(bundleEvent.getTitle());
  }

  @Test public void loadAllBundlesFromRepositoryAndLoadIntoView() {
    //Given an initialised MoreBundlePresenter
    presenter.onCreateLoadBundles();
    //When the user clicks the Home menu item
    //And loading of bundlesModel are requested
    when(moreBundleManager.loadBundle(bundleEvent.getTitle(), bundleEvent.getAction())).thenReturn(
        Single.just(bundlesModel));
    lifecycleEvent.onNext(View.LifecycleEvent.CREATE);
    //Then the progress indicator should be shown
    verify(view).showLoading();
    //Then the home should be displayed
    verify(view).showBundles(bundlesModel.getList());
    //Then the progress indicator should be hidden
    verify(view).hideLoading();
  }

  @Test public void errorLoadingBundles_ShowsError() {
    //Given an initialised MoreBundlePresenter
    presenter.onCreateLoadBundles();
    //When the loading of bundlesModel is requested
    //And an unexpected error occured
    when(moreBundleManager.loadBundle(bundleEvent.getTitle(), bundleEvent.getAction())).thenReturn(
        Single.just(new HomeBundlesModel(HomeBundlesModel.Error.GENERIC)));
    lifecycleEvent.onNext(View.LifecycleEvent.CREATE);
    //Then the generic error message should be shown in the UI
    verify(view).showGenericError();
  }

  @Test public void errorLoadingBundles_ShowsNetworkError() {
    //Given an initialised MoreBundlePresenter
    presenter.onCreateLoadBundles();
    //When the loading of bundlesModel is requested
    //And an unexpected error occured
    when(moreBundleManager.loadBundle(bundleEvent.getTitle(), bundleEvent.getAction())).thenReturn(
        Single.just(new HomeBundlesModel(HomeBundlesModel.Error.NETWORK)));
    lifecycleEvent.onNext(View.LifecycleEvent.CREATE);
    //Then the generic error message should be shown in the UI
    verify(view).showNetworkError();
  }

  @Test public void appClicked_NavigateToAppView() {
    //Given an initialised MoreBundlePresenter
    presenter.handleAppClick();
    lifecycleEvent.onNext(View.LifecycleEvent.CREATE);
    //When an app is clicked
    appClickEvent.onNext(new AppHomeEvent(aptoide, 1, localTopAppsBundle, 3, HomeEvent.Type.APP));
    //then it should navigate to the App's detail View
    verify(homeNavigator).navigateToAppView(aptoide.getAppId(), aptoide.getPackageName(),
        aptoide.getTag());
  }

  @Test public void adClicked_NavigateToAppView() {
    AdHomeEvent event = createAdHomeEvent();
    //Given an initialised MoreBundlePresenter
    presenter.handleAdClick();
    lifecycleEvent.onNext(View.LifecycleEvent.CREATE);
    //When an app is clicked
    adClickEvent.onNext(event);
    //then it should navigate to the App's detail View
    verify(homeNavigator).navigateToAppView(any());
  }

  @Test public void moreClicked_NavigateToActionView() {
    HomeEvent click = new HomeEvent(localTopAppsBundle, 0, HomeEvent.Type.MORE);
    //Given an initialised MoreBundlePresenter
    presenter.handleMoreClick();
    lifecycleEvent.onNext(View.LifecycleEvent.CREATE);
    //When more in a bundle is clicked
    moreClickEvent.onNext(click);
    //Then it should send a more clicked analytics event
    verify(homeAnalytics).sendTapOnMoreInteractEvent(0, localTopAppsBundle.getTag(),
        localTopAppsBundle.getContent()
            .size());
    //Then it should navigate with the specific action behaviour
    verify(homeNavigator).navigateWithAction(click);
  }

  @Test public void bottomReached_ShowNextBundles() {
    //Given an initialised presenter with already loaded bundlesModel into the UI before
    presenter.handleBottomReached();
    when(moreBundleManager.loadNextBundles(bundleEvent.getTitle(),
        bundleEvent.getAction())).thenReturn(Single.just(bundlesModel));
    when(moreBundleManager.hasMore(bundleEvent.getTitle())).thenReturn(true);
    lifecycleEvent.onNext(View.LifecycleEvent.CREATE);
    //When scrolling to the end of the view is reached
    //And there are more bundlesModel available to load
    bottomReachedEvent.onNext(new Object());
    //Then it should show the load more progress indicator
    verify(view).showLoadMore();
    //Then it should request the next bundlesModel to the bundlesModel repository
    verify(moreBundleManager).loadNextBundles(bundleEvent.getTitle(), bundleEvent.getAction());
    //Then it should send a endless scroll analytics event
    verify(homeAnalytics).sendLoadMoreInteractEvent();
    //Then it should hide the load more progress indicator
    verify(view).hideShowMore();
    //Then it should show the view again with old bundlesModel and added bundlesModel, retaining list position
    verify(view).showMoreHomeBundles(bundlesModel.getList());
  }

  @Test public void bottomReached_NoMoreBundlesAvailableToShow() {
    //Given an initialised presenter with already loaded bundlesModel into the UI before
    presenter.handleBottomReached();
    when(moreBundleManager.loadNextBundles(bundleEvent.getTitle(),
        bundleEvent.getAction())).thenReturn(Single.just(bundlesModel));
    when(moreBundleManager.hasMore(bundleEvent.getTitle())).thenReturn(false);
    lifecycleEvent.onNext(View.LifecycleEvent.CREATE);
    //When scrolling to the end of the view is reached
    //And there are no more bundlesModel available to load
    bottomReachedEvent.onNext(new Object());
    //Then it should do nothing
    verify(view, never()).showLoadMore();
    verify(moreBundleManager, never()).loadNextBundles(bundleEvent.getTitle(),
        bundleEvent.getAction());
    verify(view, never()).hideShowMore();
    verify(view, never()).showMoreHomeBundles(bundlesModel.getList());
  }

  @Test public void pullToRefresh_GetFreshBundles() {
    //Given an initialised presenter with already loaded bundlesModel into the UI before
    presenter.handlePullToRefresh();
    when(moreBundleManager.loadFreshBundles(bundleEvent.getTitle(),
        bundleEvent.getAction())).thenReturn(Single.just(bundlesModel));
    lifecycleEvent.onNext(View.LifecycleEvent.CREATE);
    //When pull to refresh is done
    pullToRefreshEvent.onNext(null);
    //Then a pull refresh analytics event should be sent
    verify(homeAnalytics).sendPullRefreshInteractEvent();
    //Then the progress indicator should be hidden
    verify(view).hideRefresh();
  }

  @Test public void retryClicked_LoadNextBundles() {
    //Given an initialised presenter with already loaded bundlesModel into the UI before
    presenter.handleRetryClick();
    when(moreBundleManager.loadNextBundles(bundleEvent.getTitle(),
        bundleEvent.getAction())).thenReturn(Single.just(bundlesModel));
    lifecycleEvent.onNext(View.LifecycleEvent.CREATE);
    //When pull to refresh is done
    retryClickedEvent.onNext(null);
    //Then bundles should be shown
    verify(view).showMoreHomeBundles(bundlesModel.getList());
    //Then it should hide the load more indicator (if exists)
    verify(view).hideShowMore();
    //Then it should hide the loading indicator
    verify(view).hideLoading();
  }

  @Test public void onBundleScrolledRight_SendScrollEvent() {
    //Given an initialised MoreBundlePresenter
    presenter.handleBundleScrolledRight();
    lifecycleEvent.onNext(View.LifecycleEvent.CREATE);
    //When an user scrolls a bundle with items to the right
    bundleScrolledEvent.onNext(new HomeEvent(localTopAppsBundle, 2, HomeEvent.Type.SCROLL_RIGHT));
    //Then a scroll right analytics event should be sent
    verify(homeAnalytics).sendScrollRightInteractEvent(2, localTopAppsBundle.getTag(),
        localTopAppsBundle.getContent()
            .size());
  }

  private AdHomeEvent createAdHomeEvent() {
    GetAdsResponse.Data data = new GetAdsResponse.Data();
    data.setId(0);
    data.setName("name");
    data.setRepo("repo");
    data.setPackageName("packageName");
    data.setMd5sum("md5sum");
    data.setSize(1);
    data.setVercode(2);
    data.setVername("verName");
    data.setIcon("icon");
    data.setDownloads(3);
    data.setStars(4);
    data.setDescription("description");
    data.setAdded(new Date());
    data.setModified(new Date());
    data.setUpdated(new Date());

    GetAdsResponse.Info info = new GetAdsResponse.Info();
    info.setAdId(0);
    info.setAdType("adType");
    info.setCpcUrl("cpcUrl");
    info.setCpdUrl("cpdUrl");
    info.setCpiUrl("cpiUrl");

    GetAdsResponse.Partner partner = new GetAdsResponse.Partner();
    GetAdsResponse.Partner.Data partnerData = new GetAdsResponse.Partner.Data();
    GetAdsResponse.Partner.Info partnerInfo = new GetAdsResponse.Partner.Info();
    partner.setData(partnerData);
    partner.setInfo(partnerInfo);

    GetAdsResponse.Partner tracker = new GetAdsResponse.Partner();
    tracker.setData(partnerData);
    tracker.setInfo(partnerInfo);

    GetAdsResponse.Ad ad = new GetAdsResponse.Ad();
    ad.setData(data);
    ad.setInfo(info);
    ad.setPartner(partner);
    ad.setTracker(tracker);
    AdClick adClick = new AdClick(ad, "tag");

    AdBundle adBundle =
        new AdBundle("title", new AdsTagWrapper(Collections.emptyList(), "tag2"), new Event(),
            "tag3");

    return new AdHomeEvent(adClick, 1, adBundle, 1, HomeEvent.Type.AD);
  }
}
