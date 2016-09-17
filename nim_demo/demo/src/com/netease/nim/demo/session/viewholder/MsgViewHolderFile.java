package com.netease.nim.demo.session.viewholder;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.samchat.common.SamchatOpenFileUtil;
import com.netease.nim.demo.R;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.file.FileUtil;
import com.netease.nim.demo.file.FileIcons;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderBase;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nim.uikit.session.module.list.MsgAdapter;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;

/**
 * Created by zhoujianghua on 2015/8/6.
 */
public class MsgViewHolderFile extends MsgViewHolderBase {


    private ImageView fileIcon;
    private TextView fileNameLabel;
    private TextView fileStatusLabel;
    private ProgressBar progressBar;

	private TextView message_percentage;

    private FileAttachment msgAttachment;

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_file;
    }

    @Override
    protected void inflateContentView() {
        fileIcon = (ImageView) view.findViewById(R.id.message_item_file_icon_image);
        fileNameLabel = (TextView)view.findViewById(R.id.message_item_file_name_label);
        fileStatusLabel = (TextView)view.findViewById(R.id.message_item_file_status_label);
        progressBar = (ProgressBar) view.findViewById(R.id.message_item_file_transfer_progress_bar);
		message_percentage = (TextView)view.findViewById(R.id.message_percentage);
    }

	@Override
	protected void bindContentView() {
		msgAttachment = (FileAttachment) message.getAttachment();
		String path = msgAttachment.getPath();
		initDisplay();

		AttachStatusEnum status = message.getAttachStatus();
		switch (status) {
			case def:
				updateFileStatusLabel();
			break;
			case transferring:
				fileStatusLabel.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
				progressBar.setProgress((int) (getAdapter().getProgress (message) * 100));
			break;
			case transferred:
			case fail:
				updateFileStatusLabel();
				break;
			}
    }

    private void loadImageView() {
        fileStatusLabel.setVisibility(View.VISIBLE);
        // 文件长度
        StringBuilder sb = new StringBuilder();
        sb.append(FileUtil.formatFileSize(msgAttachment.getSize()));
        fileStatusLabel.setText(sb.toString());

        progressBar.setVisibility(View.GONE);
    }

    private void initDisplay() {
        int iconResId = FileIcons.smallIcon(msgAttachment.getDisplayName());
        fileIcon.setImageResource(iconResId);
        fileNameLabel.setText(msgAttachment.getDisplayName());
    }

    private void updateFileStatusLabel() {
        fileStatusLabel.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        // 文件长度
        StringBuilder sb = new StringBuilder();
        sb.append(FileUtil.formatFileSize(msgAttachment.getSize()));
        sb.append("  ");
        // 下载状态
        String path = msgAttachment.getPathForSave();
        if(message.getDirect() == MsgDirectionEnum.In){
             if (AttachmentStore.isFileExist(path)){
                 sb.append(context.getString(R.string.samchat_file_transfer_state_downloaded));
             }else{
                 sb.append(context.getString(R.string.samchat_file_transfer_state_undownloaded));
             }
        }else{
            if(message.getAttachStatus() == AttachStatusEnum.fail){
                 sb.append(context.getString(R.string.samchat_file_transfer_state_unsend));
            }else{
                 sb.append(context.getString(R.string.samchat_file_transfer_state_send));
            }
        }

        fileStatusLabel.setText(sb.toString());
    }

    @Override
    protected int leftBackground() {
        return R.drawable.nim_message_left_white_bg;
    }

    @Override
    protected int rightBackground() {
        return R.drawable.nim_message_right_blue_bg;
    }

	@Override
	protected void onItemClick() {
		String path = msgAttachment.getPathForSave();
		LogUtil.e("test","getPath:" + msgAttachment.getPath());
		LogUtil.e("test","getPathForSave:" + msgAttachment.getPathForSave());
		LogUtil.e("test","	getThumbPath:" + msgAttachment.getThumbPath());
		LogUtil.e("test","getThumbPathForSave:" + msgAttachment.getThumbPathForSave());
		LogUtil.e("test","getMD5:" + msgAttachment.getMd5());
	
		if(path != null){
			Intent intent = SamchatOpenFileUtil.openFile(path);
			if(intent != null)
                context.startActivity(intent);
		}
	}
}
