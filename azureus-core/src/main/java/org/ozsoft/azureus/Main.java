package org.ozsoft.azureus;

import java.io.File;

public class Main implements TorrentListener {

    private static final String TORRENT_PATH = "test.torrent";

    // private static final String MAGNET_URI =
    // "magnet:?xt=urn:btih:11B23FCAD37A547F1173E12EEA6521C112C98072&dn=arrow+s03e15+hdtv+x264+lol+ettv";
    private String savePath;

    public static void main(String[] args) {
        new Main().run(args);
    }

    private void run(String[] args) {
        if (args.length < 1) {
            System.err.println("ERROR: No save path specified");
            System.exit(1);
        }
        savePath = args[0];

        TorrentManager tm = TorrentManagerFactory.createTorrentManager();

        tm.addTorrentListener(this);

        try {
            tm.start();

            for (Torrent torrent : tm.getTorrents()) {
                torrent.delete();
            }

            tm.downloadTorrent(TORRENT_PATH, savePath);

            while (tm.hasActiveDownloads()) {
                for (Torrent torrent : tm.getTorrents()) {
                    System.out.println("Torrent: " + torrent);
                }

                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    // Safe to ignore.
                }
            }

            System.out.println("No more active downloads.");

            tm.stop();

        } catch (TorrentException e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void downloadCompleted(Torrent torrent) {
        System.out.println("Download completed: " + torrent);

        for (File file : torrent.getFiles()) {
            System.out.println(file);
        }

        try {
            torrent.remove();
        } catch (TorrentException e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void downloadFailed(Torrent torrent) {
        System.err.println("Download failed: " + torrent);
        try {
            torrent.remove();
        } catch (TorrentException e) {
            e.printStackTrace(System.err);
        }
    }
}
