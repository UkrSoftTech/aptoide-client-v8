package cm.aptoide.pt.shareapps.socket.message.server;

import cm.aptoide.pt.shareapps.socket.entities.Host;
import cm.aptoide.pt.shareapps.socket.interfaces.OnError;
import cm.aptoide.pt.shareapps.socket.message.AptoideMessageController;
import cm.aptoide.pt.shareapps.socket.message.HandlersFactory;
import cm.aptoide.pt.shareapps.socket.message.Message;
import cm.aptoide.pt.shareapps.socket.message.interfaces.Sender;
import java.io.IOException;
import lombok.Getter;

/**
 * Created by neuro on 29-01-2017.
 */

public class AptoideMessageServerController extends AptoideMessageController
    implements Sender<Message> {

  @Getter private final Host host;
  @Getter private final Host localHost;

  public AptoideMessageServerController(AptoideMessageServerSocket aptoideMessageServerSocket,
      Host localHost, Host host, OnError<IOException> onError) {
    super(HandlersFactory.newDefaultServerHandlersList(aptoideMessageServerSocket), onError);
    this.localHost = localHost;
    this.host = host;
  }
}
