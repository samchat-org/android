package com.netease.nim.uikit.session.helper;

import android.text.TextUtils;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.MemberChangeAttachment;
import com.netease.nimlib.sdk.team.model.MuteMemberAttachment;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.UpdateTeamAttachment;

import java.util.List;
import java.util.Map;

/**
 * 系统消息描述文本构造器。主要是将各个系统消息转换为显示的文本内容。<br>
 * Created by huangjun on 2015/3/11.
 */
public class TeamNotificationHelper {
    private static ThreadLocal<String> teamId = new ThreadLocal<>();

    public static String getMsgShowText(final IMMessage message) {
        String content = "";
        String messageTip = message.getMsgType().getSendMessageTip();
        if (messageTip.length() > 0) {
            content += "[" + messageTip + "]";
        } else {
            if (message.getSessionType() == SessionTypeEnum.Team && message.getAttachment() != null) {
                content += getTeamNotificationText(message, message.getSessionId());
            } else {
                content += message.getContent();
            }
        }

        return content;
    }

    public static String getTeamNotificationText(IMMessage message, String tid) {
        return getTeamNotificationText(message.getSessionId(), message.getFromAccount(), (NotificationAttachment) message.getAttachment());
    }

    public static String getTeamNotificationText(String tid, String fromAccount, NotificationAttachment attachment) {
        teamId.set(tid);
        String text = buildNotification(tid, fromAccount, attachment);
        teamId.set(null);
        return text;
    }

    private static String buildNotification(String tid, String fromAccount, NotificationAttachment attachment) {
        String text;
        switch (attachment.getType()) {
            case InviteMember:
                text = buildInviteMemberNotification(((MemberChangeAttachment) attachment), fromAccount);
                break;
            case KickMember:
                text = buildKickMemberNotification(((MemberChangeAttachment) attachment));
                break;
            case LeaveTeam:
                text = buildLeaveTeamNotification(fromAccount);
                break;
            case DismissTeam:
                text = buildDismissTeamNotification(fromAccount);
                break;
            case UpdateTeam:
                text = buildUpdateTeamNotification(tid, fromAccount, (UpdateTeamAttachment) attachment);
                break;
            case PassTeamApply:
                text = buildManagerPassTeamApplyNotification((MemberChangeAttachment) attachment);
                break;
            case TransferOwner:
                text = buildTransferOwnerNotification(fromAccount, (MemberChangeAttachment) attachment);
                break;
            case AddTeamManager:
                text = buildAddTeamManagerNotification((MemberChangeAttachment) attachment);
                break;
            case RemoveTeamManager:
                text = buildRemoveTeamManagerNotification((MemberChangeAttachment) attachment);
                break;
            case AcceptInvite:
                text = buildAcceptInviteNotification(fromAccount, (MemberChangeAttachment) attachment);
                break;
            case MuteTeamMember:
                text = buildMuteTeamNotification((MuteMemberAttachment) attachment);
                break;
            default:
                text = getTeamMemberDisplayName(fromAccount) + ": unknown message";
                break;
        }

        return text;
    }

    private static String getTeamMemberDisplayName(String account) {
        return TeamDataCache.getInstance().getTeamMemberDisplayNameYou(teamId.get(), account);
    }

    private static String buildMemberListString(List<String> members, String fromAccount) {
        StringBuilder sb = new StringBuilder();
        for (String account : members) {
            if (!TextUtils.isEmpty(fromAccount) && fromAccount.equals(account)) {
                continue;
            }
            sb.append(getTeamMemberDisplayName(account));
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    private static String buildInviteMemberNotification(MemberChangeAttachment a, String fromAccount) {
        StringBuilder sb = new StringBuilder();
        String selfName = getTeamMemberDisplayName(fromAccount);

        sb.append(selfName);
        sb.append(NimUIKit.getContext().getString(R.string.samchat_invite)+" ");
        sb.append(buildMemberListString(a.getTargets(), fromAccount));
        Team team = TeamDataCache.getInstance().getTeamById(teamId.get());
        if (team.getType() == TeamTypeEnum.Advanced) {
            sb.append(" "+NimUIKit.getContext().getString(R.string.samchat_join_advance_group));
        } else {
            sb.append(" "+NimUIKit.getContext().getString(R.string.samchat_join_group));
        }

        return sb.toString();
    }

    private static String buildKickMemberNotification(MemberChangeAttachment a) {
        StringBuilder sb = new StringBuilder();
        sb.append(buildMemberListString(a.getTargets(), null));
        Team team = TeamDataCache.getInstance().getTeamById(teamId.get());
        if (team.getType() == TeamTypeEnum.Advanced) {
            sb.append(" "+NimUIKit.getContext().getString(R.string.samchat_remove_out_of_advance_group));
        } else {
            sb.append(" "+NimUIKit.getContext().getString(R.string.samchat_remove_out_of_group));
        }


        return sb.toString();
    }

    private static String buildLeaveTeamNotification(String fromAccount) {
        String tip;
        Team team = TeamDataCache.getInstance().getTeamById(teamId.get());
        if (team.getType() == TeamTypeEnum.Advanced) {
            tip = " "+NimUIKit.getContext().getString(R.string.samchat_leave_advance_group);
        } else {
            tip = " "+NimUIKit.getContext().getString(R.string.samchat_leave_group);
        }
        return getTeamMemberDisplayName(fromAccount) + tip;
    }

    private static String buildDismissTeamNotification(String fromAccount) {
        return getTeamMemberDisplayName(fromAccount) + " "+NimUIKit.getContext().getString(R.string.samchat_dimiss_group);
    }

    private static String buildUpdateTeamNotification(String tid, String account, UpdateTeamAttachment a) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<TeamFieldEnum, Object> field : a.getUpdatedFields().entrySet()) {
            if (field.getKey() == TeamFieldEnum.Name) {
                sb.append(NimUIKit.getContext().getString(R.string.samchat_group_rename_to)+" " + field.getValue());
            } else if (field.getKey() == TeamFieldEnum.Introduce) {
                sb.append(NimUIKit.getContext().getString(R.string.samchat_group_description_rename_to)+" " + field.getValue());
            } else if (field.getKey() == TeamFieldEnum.Announcement) {
                sb.append(TeamDataCache.getInstance().getTeamMemberDisplayNameYou(tid, account) + " "+NimUIKit.getContext().getString(R.string.samchat_modify_group_announcement));
            } else if (field.getKey() == TeamFieldEnum.VerifyType) {
                VerifyTypeEnum type = (VerifyTypeEnum) field.getValue();
                String authen = NimUIKit.getContext().getString(R.string.samchat_authentication_change_to);
                if (type == VerifyTypeEnum.Free) {
                    sb.append(authen + NimUIKit.getContext().getString(R.string.team_allow_anyone_join));
                } else if (type == VerifyTypeEnum.Apply) {
                    sb.append(authen + NimUIKit.getContext().getString(R.string.team_need_authentication));
                } else {
                    sb.append(authen + NimUIKit.getContext().getString(R.string.team_not_allow_anyone_join));
                }
            } else if (field.getKey() == TeamFieldEnum.Extension) {
                sb.append(NimUIKit.getContext().getString(R.string.samchat_extension_change_to)+" " + field.getValue());
            } else if (field.getKey() == TeamFieldEnum.Ext_Server) {
                sb.append(NimUIKit.getContext().getString(R.string.samchat_extension_server_change_to)+" " + field.getValue());
            } else if (field.getKey() == TeamFieldEnum.ICON) {
                sb.append(NimUIKit.getContext().getString(R.string.samchat_avatar_update));
            } else if (field.getKey() == TeamFieldEnum.InviteMode) {
                sb.append(NimUIKit.getContext().getString(R.string.samchat_invite_permission_change_to)+" " + field.getValue());
            } else if (field.getKey() == TeamFieldEnum.TeamUpdateMode) {
                sb.append(NimUIKit.getContext().getString(R.string.samchat_document_update_permission_change_to)+" " + field.getValue());
            } else if (field.getKey() == TeamFieldEnum.BeInviteMode) {
                sb.append(NimUIKit.getContext().getString(R.string.samchat_be_invite_authentication_change_to)+" " + field.getValue());
            } else if (field.getKey() == TeamFieldEnum.TeamExtensionUpdateMode) {
                sb.append(NimUIKit.getContext().getString(R.string.samchat_extension_modify_permission_change_to)+" " + field.getValue());
            } else {
                sb.append(NimUIKit.getContext().getString(R.string.samchat_group) + field.getKey() 
				+ NimUIKit.getContext().getString(R.string.samchat_changed_to)+" " + field.getValue());
            }
            sb.append("\r\n");
        }
        if (sb.length() < 2) {
            return NimUIKit.getContext().getString(R.string.samchat_unkown);
        }
        return sb.delete(sb.length() - 2, sb.length()).toString();
    }

    private static String buildManagerPassTeamApplyNotification(MemberChangeAttachment a) {
        StringBuilder sb = new StringBuilder();
        sb.append(NimUIKit.getContext().getString(R.string.samchat_admin_pass)+" ");
        sb.append(buildMemberListString(a.getTargets(), null));
        sb.append(" "+NimUIKit.getContext().getString(R.string.samchat_invite));

        return sb.toString();
    }

    private static String buildTransferOwnerNotification(String from, MemberChangeAttachment a) {
        StringBuilder sb = new StringBuilder();
        sb.append(getTeamMemberDisplayName(from));
        sb.append(" "+NimUIKit.getContext().getString(R.string.samchat_transfer_group_to)+" ");
        sb.append(buildMemberListString(a.getTargets(), null));

        return sb.toString();
    }

    private static String buildAddTeamManagerNotification(MemberChangeAttachment a) {
        StringBuilder sb = new StringBuilder();

        sb.append(buildMemberListString(a.getTargets(), null));
        sb.append(" "+NimUIKit.getContext().getString(R.string.samchat_appoint_as_admin));

        return sb.toString();
    }

    private static String buildRemoveTeamManagerNotification(MemberChangeAttachment a) {
        StringBuilder sb = new StringBuilder();

        sb.append(buildMemberListString(a.getTargets(), null));
        sb.append(" "+NimUIKit.getContext().getString(R.string.samchat_revocation_from_admin));

        return sb.toString();
    }

    private static String buildAcceptInviteNotification(String from, MemberChangeAttachment a) {
        StringBuilder sb = new StringBuilder();

        sb.append(getTeamMemberDisplayName(from));
        sb.append(" "+NimUIKit.getContext().getString(R.string.samchat_accept)+" ").append(buildMemberListString(a.getTargets(), null)).append(" "+NimUIKit.getContext().getString(R.string.samchat_invite));

        return sb.toString();
    }

    private static String buildMuteTeamNotification(MuteMemberAttachment a) {
        StringBuilder sb = new StringBuilder();

        sb.append(buildMemberListString(a.getTargets(), null));
        sb.append(a.isMute() ? NimUIKit.getContext().getString(R.string.samchat_forbid) : NimUIKit.getContext().getString(R.string.samchat_unforbid));

        return sb.toString();
    }
}
