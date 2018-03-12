package cm.aptoide.pt.home;

import android.support.annotation.NonNull;
import cm.aptoide.pt.dataprovider.model.v7.GetStoreWidgets;
import cm.aptoide.pt.dataprovider.model.v7.Layout;
import cm.aptoide.pt.dataprovider.model.v7.ListApps;
import cm.aptoide.pt.dataprovider.model.v7.Type;
import cm.aptoide.pt.dataprovider.model.v7.listapp.App;
import cm.aptoide.pt.view.app.Application;
import cm.aptoide.pt.view.app.FeatureGraphicApplication;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rx.functions.Func1;

/**
 * Created by jdandrade on 08/03/2018.
 */

public class BundlesResponseMapper {
  @NonNull Func1<GetStoreWidgets, List<AppBundle>> map() {
    return bundlesResponse -> fromWidgetsToBundles(bundlesResponse.getDataList()
        .getList());
  }

  private List<AppBundle> fromWidgetsToBundles(List<GetStoreWidgets.WSWidget> widgetBundles) {

    List<AppBundle> appBundles = new ArrayList<>();

    for (GetStoreWidgets.WSWidget widget : widgetBundles) {
      GetStoreWidgets.WSWidget.Data data = widget.getData();
      if (data == null) {
        continue;
      }
      AppBundle.BundleType type = bundleTypeMapper(widget.getType(), data.getLayout());
      if (type.equals(AppBundle.BundleType.APPS) || type.equals(AppBundle.BundleType.EDITORS)) {
        try {
          appBundles.add(new AppBundle(widget.getTitle(), applicationsToApps(
              ((ListApps) widget.getViewObject()).getDataList()
                  .getList(), type), type));
        } catch (Exception ignore) {
        }
      }
    }

    return appBundles;
  }

  private AppBundle.BundleType bundleTypeMapper(Type type, Layout layout) {
    switch (type) {
      case APPS_GROUP:
        if (layout.equals(Layout.BRICK)) {
          return AppBundle.BundleType.EDITORS;
        } else {
          return AppBundle.BundleType.APPS;
        }
      default:
        return AppBundle.BundleType.APPS;
    }
  }

  private List<Application> applicationsToApps(List<App> apps, AppBundle.BundleType type) {
    if (apps == null || apps.isEmpty()) {
      return Collections.EMPTY_LIST;
    }
    List<Application> applications = new ArrayList<>();
    for (App app : apps) {
      if (type.equals(AppBundle.BundleType.EDITORS)) {
        applications.add(new FeatureGraphicApplication(app.getName(), app.getIcon(), app.getStats()
            .getRating()
            .getAvg(), app.getStats()
            .getPdownloads(), app.getPackageName(), app.getId(), app.getGraphic()));
      } else {
        applications.add(new Application(app.getName(), app.getIcon(), app.getStats()
            .getRating()
            .getAvg(), app.getStats()
            .getPdownloads(), app.getPackageName(), app.getId()));
      }
    }

    return applications;
  }
}