/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 28/07/2016.
 */

package cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid;

import android.content.Context;

import cm.aptoide.pt.actions.PermissionRequest;
import cm.aptoide.pt.database.realm.Download;
import cm.aptoide.pt.database.realm.Rollback;
import cm.aptoide.pt.model.v7.Type;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.install.InstallManager;
import cm.aptoide.pt.v8engine.util.DownloadFactory;
import cm.aptoide.pt.v8engine.view.recycler.displayable.DisplayablePojo;
import io.realm.Realm;
import lombok.Getter;
import rx.Observable;

/**
 * Created by sithengineer on 14/06/16.
 */
public class RollbackDisplayable extends DisplayablePojo<Rollback> {

	private InstallManager installManager;

	public RollbackDisplayable() { }

	public RollbackDisplayable(InstallManager installManager, Rollback pojo) {
		this(installManager, pojo, false);
	}

	public RollbackDisplayable(InstallManager installManager, Rollback pojo, boolean fixedPerLineCount) {
		super(pojo, fixedPerLineCount);
		this.installManager = installManager;
	}

	public Download getDownloadFromPojo() {
		return new DownloadFactory().create(getPojo());
	}

	@Override
	public Type getType() {
		return Type.ROLLBACK;
	}

	@Override
	public int getViewLayout() {
		return R.layout.rollback_row;
	}

	public Observable<Void> install(Context context, PermissionRequest permissionRequest, long appId) {
		return installManager.install(context, permissionRequest, appId);
	}

	public Observable<Void> uninstall(Context context, Download appDownload) {
		return installManager.uninstall(context, appDownload.getFilesToDownload().get(0).getPackageName());
	}

	public Observable<Void> downgrade(Context context, PermissionRequest permissionRequest, Download currentDownload, long previousAppId) {
		return Observable.concat(uninstall(context, currentDownload).ignoreElements(), install(context, permissionRequest, previousAppId));
	}
}
