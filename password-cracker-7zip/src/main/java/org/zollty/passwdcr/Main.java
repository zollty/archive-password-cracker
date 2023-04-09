package org.zollty.passwdcr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.jretty.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 破解7-zip密码
 * 
 * @author zollty
 * @since 2023年4月8日
 */
public class Main {

    static final Logger log = LoggerFactory.getLogger("ROOT");

    public static void main(String[] args) throws Exception {
        String zipPath = "E:\\SS源码\\shanshuo-yang\\shanshuo\\7zcracker\\aix2john.7z";
        String passwdFileOrDirPath = "D:\\PasswordDic\\top25";
        String startFrom = null;
        if (args.length > 0) {
            zipPath = args[0];
            passwdFileOrDirPath = args[1];
            if (args.length > 2) {
                startFrom = args[2];
            }
        }

        Collection<String> psl = null;
        if (passwdFileOrDirPath.contains("/") || passwdFileOrDirPath.contains("\\")) {
            File file = new File(passwdFileOrDirPath);
            if (file.isDirectory()) {
                List<File> lsf = FileUtils.loopFiles(file);
                psl = new LinkedHashSet<>();
                for (File f : lsf) {
                    psl.addAll(FileUtils.getTextFileContent(new FileInputStream(f), null));
                }
                lsf = null;
            } else {
                psl = FileUtils.getTextFileContent(passwdFileOrDirPath, null);
            }

            if (startFrom != null) {
                Set<String> tpsl = new LinkedHashSet<>();
                boolean add = false;
                for (String m : psl) {
                    if (add) {
                        tpsl.add(m);
                    } else if (startFrom.equals(m)) {
                        add = true;
                    }
                }
                psl = tpsl;
            }

        } else {
            psl = Arrays.asList(passwdFileOrDirPath);
        }

        log.info("ready to 破解 {}，密码数量：{}", zipPath, psl.size());
        testPasswd(zipPath, psl);
    }

    static boolean testPasswd(String sourceRarPath, Collection<String> passWord) {
        File srcFile = new File(sourceRarPath);// 获取当前压缩文件
        int i = 0;
        SevenZFile zIn = null;
        for (String passwd : passWord) {
            i++;
            try {
                zIn = new SevenZFile(srcFile, passwd.toCharArray());
                log.info("外层破解成功：passwd={}, path: {}", passwd, sourceRarPath);
                break;
            } catch (IOException e) {
                if (i % 500 == 0) {
                    log.info("外层破解失败次数：{}, passwd={}, info: {}", i, passwd, e.getMessage());
                }
            }
        }
        if (zIn == null) {
            log.warn("外层破解失败：{}", sourceRarPath);
            return false;
        }
        try {
            boolean ret = false;
            i = 0;
            SevenZArchiveEntry entry = null;
            while ((entry = zIn.getNextEntry()) != null) {
                if (entry.getSize() > 0) {
                    log.info("准备破解文件：{} {}", zIn.getcurrentEntryIndex(), entry.getName());
                    for (String passwd : passWord) {
                        zIn.seekPre();
                        zIn.setPassword(passwd.toCharArray());
                        entry = zIn.getNextEntry();
                        if (validStream(++i, entry, passwd, zIn)) {
                            if (entry.getCrcValue() == CRCUtils.loadCRC32(zIn.getInputStream(entry)).getValue()) {
                                log.warn("恭喜，找到密码：" + passwd);
                                ret = true;
                                break;
                            }
                        }
                    }
                    break; // 只取第一个文件
                }
            }
            zIn.close();
            log.info("尝试次数：" + i + "，最终结果: " + ret);
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean validStream(int i, SevenZArchiveEntry entry, String passwd, SevenZFile zIn) {
        try {
            zIn.getcurrentFolderIndex();
            zIn.getdeferredBlockStreams();
            InputStream is = zIn.getInputStream(entry);
            if (entry.getSize() > 1000) {
                is.read(new byte[1000]);
            } else {
                is.read(new byte[(int) entry.getSize() - 1]);
            }
            is.close();
            log.info("尝试次数：" + i + "，破解成功，密码为: " + passwd);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}