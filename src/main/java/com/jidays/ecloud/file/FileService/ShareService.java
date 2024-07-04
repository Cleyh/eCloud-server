package com.jidays.ecloud.file.FileService;

import com.jidays.ecloud.DTO.MoveFileDTO;
import com.jidays.ecloud.DTO.ShareFileDTO;
import com.jidays.ecloud.Entity.EFile;
import com.jidays.ecloud.Entity.Share;
import com.jidays.ecloud.file.fileMapper.EFileMapper;
import com.jidays.ecloud.file.fileMapper.ShareMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShareService {
    @Autowired
    ShareMapper shareMapper;
    @Autowired
    EFileMapper eFileMapper;
    @Autowired
    FileService fileService;

    public boolean createShare(ShareFileDTO shareFileDTO, Integer userID) {
        EFile file = eFileMapper.getFileById(shareFileDTO.getFileID());
        if (file == null || file.getUser_id() != userID) {
            return false;
        }

        List<Integer> userIDs = shareFileDTO.isLimited() ? shareFileDTO.getUserIDs() : null;
        String password = shareFileDTO.isNeedPassword() ? shareFileDTO.getPassword() : "";
        String expireTime = shareFileDTO.isExpandable() ? shareFileDTO.getExpireTime() : null;
        boolean isDisposable = shareFileDTO.isDisposable();

        if (userIDs == null) {
            // 公开的分享
            shareMapper.createShare(
                    file.getFile_id(),
                    password,
                    expireTime,
                    isDisposable
            );
        } else {
            // 指定人的分享
            shareMapper.createMultipleShare(
                    file.getFile_id(),
                    password,
                    expireTime,
                    isDisposable,
                    userIDs
            );
        }
        return true;
    }

    public String downloadShare(Integer shareID, Integer userID, int longMode) {
        Share share = shareMapper.getShare(shareID);
        if (share == null || share.isDeleted()) {
            return null;
        } else if (share.getUser_id() != userID && share.getUser_id() != 0) {
            EFile file = eFileMapper.getFileById(share.getFile_id());
            if (file == null || file.getUser_id() != userID) {
                return null;
            }
        }

        // 检查分享是否过期
        if (share.getExpire_time() != null && share.getExpire_time().isEmpty()) {
            LocalDateTime expireTime = LocalDateTime.parse(share.getExpire_time());
            if (LocalDateTime.now().isAfter(expireTime)) {
                return null;
            }
        }

        if (share.isDisposable()) {
            shareMapper.deleteShare(shareID);
            return fileService.getFileDownload(share.getFile_id(), userID);
        }

        // 长链接模式 : 1
        if (longMode == 1) {
            String token = shareMapper.getDownloadToken(shareID);
            if (token == null || token.isEmpty()) {
                token = fileService.getFileDownload(share.getFile_id(), userID);
                shareMapper.updateDownloadToken(shareID, token);
            }
            return token;
        } else {
            return fileService.getFileDownload(share.getFile_id(), userID);
        }
    }

    public List<Share> getSharedFiles(Integer userID) {
        return shareMapper.getSharedFiles(userID);
    }

    public List<Share> getMySharedFiles(Integer userID) {
        return shareMapper.getMySharedFiles(userID);
    }

    public boolean deleteShare(Integer shareID) {
        Share share = shareMapper.getShare(shareID);
        if (share == null || share.isDeleted()) {
            return false;
        }
        shareMapper.deleteShare(shareID);
        return true;
    }

    public boolean saveToDrive(Integer shareID, Integer userID) {
        Share share = shareMapper.getShare(shareID);
        if (share == null || share.isDeleted()) {
            return false;
        }
        if (share.getUser_id() == userID) {
            EFile file = eFileMapper.getFileById(share.getFile_id());
            if (file == null) {
                return false;
            }
            return fileService.cloneFile(new MoveFileDTO(share.getFile_id(), file.getParent_id()), userID);
        }
        return false;
    }
}
