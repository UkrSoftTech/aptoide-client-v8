package cm.aptoide.pt.spotandshareapp.view;

import cm.aptoide.pt.spotandshareapp.AppModel;
import cm.aptoide.pt.v8engine.presenter.View;
import java.util.List;
import rx.Observable;

/**
 * Created by filipe on 12-06-2017.
 */

public interface SpotAndShareAppSelectionView extends View {

  void finish();

  void setupRecyclerView(List<AppModel> installedApps);

  Observable<Void> backButtonEvent();

  void showExitWarning();

  Observable<Void> exitEvent();

  void navigateBack();

  void onLeaveGroupError();

  Observable<AppModel> appSelection();

  void openTransferRecord();
}
