package com.wowfly.wowyun.wowyun_device;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Created by user on 8/14/14.
 */
public class XMPPService {
    private static final String TAG = "XMPPService";
    private  ConnectionConfiguration mConfig;
    private  Connection mConn;
    private  boolean bLogin = false;
    private  boolean bBuddyData = false;
    private Map<String, ChatManager> mChatMap;
    private Collection<RosterEntry> mRECollection;
    private List<Map<String, Object>> mBuddyDataList;
    private RosterListener rosterListener;
    private MainActivity mainActivity;

    public static class BuddyInfo {
        String name;
        String jid;
        boolean isAvailable;
    }
    private String mMyJid = "null";

    private ArrayList<BuddyInfo> mBuddyInfo;

    public XMPPService() {
        mBuddyDataList = new ArrayList<Map<String, Object>>();
        mBuddyInfo = new ArrayList<BuddyInfo>(16);

/*        rosterListener = new RosterListener() {
            @Override
            public void entriesAdded(Collection<String> strings) {

            }

            @Override
            public void entriesUpdated(Collection<String> strings) {

            }

            @Override
            public void entriesDeleted(Collection<String> strings) {

            }

            @Override
            public void presenceChanged(Presence presence) {

            }
        };*/

        try {
            mConfig = new ConnectionConfiguration("wuruxu.com", 5222);
            mConfig.setReconnectionAllowed(true);
            mConfig.setSendPresence(true);
            mConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            mConfig.setCompressionEnabled(false);
            mConfig.setSelfSignedCertificateEnabled(false);
            mConfig.setSASLAuthenticationEnabled(false);
            mConfig.setVerifyChainEnabled(false);
            mConfig.setRosterLoadedAtLogin(true);
            mConn = new XMPPConnection(mConfig);
            //mConfig.
            mConn.connect();
            mChatMap = new HashMap<String, ChatManager>();
            mConn.getRoster().setSubscriptionMode(Roster.SubscriptionMode.manual);
            mConn.addPacketListener(new PacketListener() {
                @Override
                public void processPacket(Packet packet) {
                    if(packet instanceof Presence) {
                        //Log.i(TAG, " Presence packet received");
                        Presence presence = (Presence) packet;
                        //Log.i(TAG, " xml = " + presence.toXML());
                    }
                }
            }, new PacketFilter() {
                @Override
                public boolean accept(Packet packet) {
                    if(packet instanceof Presence) {
                        Presence presence = (Presence) packet;
                        Log.i(TAG, " PacketFilter Presence accept " + presence.getFrom());
                        if(presence.getType().equals(Presence.Type.subscribed)) {
                            return true;
                        }
                        if(presence.getType().equals(Presence.Type.subscribe)) {
                            Presence subscribed = new Presence(Presence.Type.subscribed);
                            subscribed.setTo(presence.getFrom());
                            mConn.sendPacket(subscribed);

                            subscribed = new Presence(Presence.Type.subscribe);
                            subscribed.setTo(presence.getFrom());
                            mConn.sendPacket(subscribed);
                            Message msg = new Message();
                            msg.what = WowYunApp.XMPP_NEW_BUDDY;
                            mainActivity.mHandler.sendMessage(msg);
                            return true;
                        }
                        if(presence.getType().equals(Presence.Type.unsubscribe))
                            return true;
                        if(presence.getType().equals(Presence.Type.unsubscribed))
                            return true;
                    }
                    return false;
                }
            });
        } catch (XMPPException e) {
            Log.e(TAG, "XMPPService e = " + e.getMessage());
        }
    }

    public void setMainActivity(MainActivity activity) {
        mainActivity = activity;
    }

    public void addRosterListener(RosterListener listener) {
        if(mConn.isConnected()) {
            Log.i(TAG, "add new roster listener ");
            mConn.getRoster().addRosterListener(listener);
        }
    }

    public int getBuddyCount() {
        return mBuddyInfo.size();
    }

    public BuddyInfo getBuddyInfo(int pos) {
        return mBuddyInfo.get(pos);
    }

    public void getBuddyList() {
        Roster r = mConn.getRoster();

        //r.reload();

        mRECollection = r.getEntries();

        mBuddyDataList.clear();
        mBuddyInfo.clear();

        for(RosterEntry entry: mRECollection) {
            Log.i(TAG, "name = " + entry.getName() + " user = " + entry.getUser());
            BuddyInfo info = new BuddyInfo();
            info.name = entry.getName();
            info.jid = entry.getUser();
            info.isAvailable = r.getPresence(info.jid).isAvailable();
            if(info.name == null) {
                int sep = info.jid.lastIndexOf('@');
                info.name = info.jid.substring(0, sep);
            }
            mBuddyInfo.add(info);

            Map<String, Object> item = new HashMap<String, Object>();
            item.put("jid", info.jid);
            if(info.isAvailable)
                item.put("icon", R.drawable.ic_buddy_online);
            else
                item.put("icon", R.drawable.ic_buddy_offline);

            if(info.name != null)
                item.put("name", info.name);
            else
                item.put("name", info.jid);
            mBuddyDataList.add(item);
        }
        Log.i(TAG, " getBuddyList.buddysize = " + mBuddyInfo.size());
        bBuddyData = true;
    }

    public List<Map<String, Object>> getBuddyData() {
        Log.i(TAG, " getBuddyData bLogin = " + bLogin + " size = " + mBuddyDataList.size());
        if(bBuddyData==false)
            getBuddyList();
        if(bLogin) {
            return mBuddyDataList;
        }
        return null;
    }

    public String getMyJid() {
        return mMyJid;
    }

    public boolean addBuddybyJid(String jid) {
        if(mConn.isConnected() == true) {
            try {
                Roster r = mConn.getRoster();
                r.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
                r.createEntry(jid+"@wuruxu.com", jid, null);
                return true;
            } catch (XMPPException e) {
                Log.e(TAG, "add Buddy failure " + e.getMessage());
            }
        }
        return false;
    }

    public void doInit() {
        bBuddyData = false;
        mBuddyDataList.clear();
        mBuddyInfo.clear();
/*        try {
            mRECollection.clear();
        } catch (UnsupportedOperationException e) {
            Log.i(TAG, "mRECollection.clear not needed");
        }*/

        if(mConn.isConnected()) {
            try {
                mConn.disconnect();
                mConn.connect();
            }catch (XMPPException e) {
                Log.i(TAG, "doInit failure");
            }
        }
    }

    public boolean isConnected() {
        if(mConn != null)
            return mConn.isConnected();
        else
            return false;
    }
    public boolean doLogin(String user, String passwd, ChatManagerListener listener) {
        try {
            if(mConn.isConnected() == false) {
                mConn.connect();
            }

            mConn.login(user, passwd, "Smack");
            bLogin = true;
            if(listener != null) {
                ChatManager cm = mConn.getChatManager();
                cm.addChatListener(listener);
            }
            VCard vCard = new VCard();
            vCard.setNickName(passwd);
            vCard.setOrganization("WOWFLY");
            vCard.setOrganizationUnit("WOWYUN");
            vCard.save(mConn);
            mMyJid = user;
            return true;
        } catch (XMPPException e ) {
            Log.e(TAG, "doLogin XMPPException " + e.getMessage());
        } catch (IllegalStateException e) {
            if( e.getMessage().equals("Already logged in to server.")) {
                //Log.e(TAG, "already logged in to server.");
                bLogin = true;
                mMyJid = user;
                return  true;
            }
        }
        return false;
    }

    public boolean setChatManagerListener(ChatManagerListener listener) {
        if(listener != null) {
            ChatManager cm = mConn.getChatManager();
            cm.addChatListener(listener);
            return true;
        }

        return false;
    }

    public boolean doRegister(String username, String passwd) {
        try {
            AccountManager am = mConn.getAccountManager();
            Map<String, String> attr = new HashMap<String, String>();
            attr.put("username", username);
            attr.put("password", passwd);
            attr.put("email", username + "@wuruxu.com");

            am.createAccount(username, passwd, attr);
            return true;
        } catch (XMPPException e) {
            Log.e(TAG, "doRegister " + username + " " + e.getMessage());
            if(e.getMessage().equals("conflict(409)")) {
                Log.i(TAG, "account " + username + " has registered");
                return true;
            }
        }
        return false;
    }

    public boolean isLogin() {
        return bLogin;
    }

    public boolean doDelete() {
        if(bLogin) {
            try {
                AccountManager am = mConn.getAccountManager();
                am.deleteAccount();
            } catch (XMPPException e) {
                Log.e(TAG, "doDelete " + e.getMessage());
            }

            return true;
        }
        return false;
    }

/*    public void doChat(String to, ChatManagerListener MsgListener) {
        if(bLogin) {
            ChatManager cm = mConn.getChatManager();
            //WeakReference wr = new WeakReference(cm);
            cm.addChatListener(MsgListener);
            mChatMap.put(to, cm);

        }
    }*/

    public boolean doSend(String to, String msg) {
        if(bLogin) {
            try {
                ChatManager cm = mConn.getChatManager();
                Chat chat = cm.createChat(to, null);
                chat.sendMessage(msg);
            } catch(XMPPException e) {
                Log.e(TAG, "doSend " + e.getMessage());
            }
            return true;
        }

        return false;
    }
}

