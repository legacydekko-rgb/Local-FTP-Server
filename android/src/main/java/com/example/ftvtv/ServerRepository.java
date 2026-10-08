package com.example.ftvtv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ============================================================================
 * FILE: app/src/main/java/com/example/ftvtv/ServerRepository.java
 * ============================================================================
 * >>> THIS IS THE ONLY FILE YOU NEED TO EDIT TO CHANGE YOUR SERVER LIST <<<
 *
 * Add a line to the SERVERS list, rebuild, done. The dashboard grid, the
 * D-Pad order and the WebView all read from here automatically.
 *
 * Tips
 *  - Always keep the trailing slash: "http://site.com/" (not "http://site.com")
 *    so relative folder links resolve correctly inside the WebView.
 *  - The LAN addresses (172.16.x.x / 10.16.x.x) only work while the device is on
 *    the same Wi-Fi/Ethernet network as the server.
 * ============================================================================
 */
public final class ServerRepository {

    private ServerRepository() {
        // no instances
    }

    private static final List<Server> SERVERS = new ArrayList<Server>();

    static {
        // name, tagline, url, initials, accent colour (0xFFRRGGBB)
        
        // --- ফিল্টার করা ইউনিক সার্ভার তালিকা ---
        SERVERS.add(new Server("Dhaka Flix",    "Movies & Series",      "http://172.16.50.14/",    "DF", 0xFF7C3AED));
        SERVERS.add(new Server("Local Server",  "LAN Media Server",     "http://10.16.100.244/",   "LS", 0xFF059669));
        SERVERS.add(new Server("CrazyCTG",      "Movies & Series",      "http://crazyctg.com/",    "CC", 0xFFDC2626));
        SERVERS.add(new Server("KhulnaPlex",    "Movies & Series",      "http://khulnaplex.net/",  "KP", 0xFFEA580C));
        SERVERS.add(new Server("IBCCL FTP",     "Movies & FTP Server",  "http://103.203.93.2/",    "IB", 0xFF0284C7));
        SERVERS.add(new Server("BCI TV",        "Live TV & IPTV",       "https://bciptv.net/",     "BC", 0xFFD97706));
        SERVERS.add(new Server("BDIX List React","FTP Directory",       "https://bdix-server-list-react.netlify.app/", "BR", 0xFF0D9488));
        SERVERS.add(new Server("MyBDIX",        "BDIX Tester & Links",  "https://mybdix.com/",     "MB", 0xFF4F46E5));
        SERVERS.add(new Server("Local Dashboard","Media Dashboard",     "http://10.16.100.244/dashboard.php?session=9b0df1b9dc89ec2acd71444b7f8eec1614eb934eacd6028dc3909890e8decdc3/", "LD", 0xFF16A34A));
        SERVERS.add(new Server("Discovery FTP", "Movies & Media",       "https://movies.discoveryftp.net/m/", "DF", 0xFF9333EA));
        SERVERS.add(new Server("CTG Movies",    "Chittagong FTP",       "http://ctgmovies.com/",   "CM", 0xFFE11D48));
        SERVERS.add(new Server("SAM FTP",       "SAM FTP Media",        "http://samftp.com/",      "SF", 0xFF7C3AED));
        SERVERS.add(new Server("FTPBD",         "Popular BDIX Media",   "http://ftpbd.net/",       "FB", 0xFF2563EB));
        SERVERS.add(new Server("Cognet FTP",    "BDIX High Speed",      "http://103.153.175.254/", "CN", 0xFF059669));
        SERVERS.add(new Server("Carnival FTP",  "Carnival Internet FTP","http://103.106.238.74/", "CA", 0xFFD97706));
        SERVERS.add(new Server("Cinehub24",     "Link3 FTP Media",      "http://www.cinehub24.com/","CH", 0xFF4F46E5));
        SERVERS.add(new Server("Dhaka FTP",     "Dhaka Media Server",   "http://dhakaftp.com/",    "DF", 0xFF0284C7));
        SERVERS.add(new Server("FuntimeBD",     "Entertainment FTP",    "http://funtimebd.com/",   "FT", 0xFFDB2777));
        SERVERS.add(new Server("Quick Online",  "Quick Online FTP",     "http://quickonlineftp.com/","QO", 0xFF16A34A));
        SERVERS.add(new Server("DFNBD",         "DFN Media Network",    "http://bn.dfnbd.net/",    "DN", 0xFF7C3AED));
        SERVERS.add(new Server("NetAtHome",     "Net At Home FTP",      "http://www.netathomebd.com/","NH", 0xFFEA580C));
        SERVERS.add(new Server("Video Mela",    "Movies & TV Shows",    "http://vdomela.com/",     "VM", 0xFFDC2626));
        SERVERS.add(new Server("iHub Live",     "Live Entertainment",   "http://ihub.live/",       "IH", 0xFF0D9488));
        SERVERS.add(new Server("Bongo BD",      "Streaming Platform",   "https://www.bongobd.com/en/","BB", 0xFFE11D48));
        SERVERS.add(new Server("iFlix HD",      "HD Movies FTP",        "https://iflixhd.top/",    "IF", 0xFF2563EB));
        SERVERS.add(new Server("HD iFlix",      "HD Movies & Series",   "https://hdiflix.top/",    "HI", 0xFF059669));
        SERVERS.add(new Server("Showtime BD",   "Showtime Movies",      "http://showtimebd.com/",  "ST", 0xFFD97706));
        SERVERS.add(new Server("Movie Mela",    "Movie Mela Live",      "http://www.moviemela.live/","MM", 0xFF9333EA));
        SERVERS.add(new Server("FS Ebox",       "Ebox Media Server",    "http://fs.ebox.live/",    "FE", 0xFF0284C7));
        SERVERS.add(new Server("Movie Box",     "Movie Box BD",         "http://movieboxbd.com/",  "MB", 0xFF4F46E5));
        SERVERS.add(new Server("Movie Haat",    "Movie Haat Media",     "http://www.moviehaat.net/","MH", 0xFF16A34A));
        SERVERS.add(new Server("Media Gallery", "Media Gallery Server", "http://58.84.34.38/",      "MG", 0xFF059669));
        SERVERS.add(new Server("FTP Media",     "LAN FTP Media",        "http://10.1.1.1/",        "FM", 0xFF2563EB));
        SERVERS.add(new Server("Natural BD",    "Natural BD FTP",       "http://www.naturalbd.com/","NB", 0xFFEA580C));
        SERVERS.add(new Server("Nagordola",     "Nagordola FTP",        "http://www.nagordola.com.bd/","ND", 0xFFDC2626));
        SERVERS.add(new Server("Tajpata FTP",   "Tajpata File Server",  "http://file.tajpata.com/?dir=English%20Movie/%282020%29", "TP", 0xFF0D9488));
        SERVERS.add(new Server("Kloud Movies",  "Cloud Movie Server",   "http://movies.kloud.com.bd/","KM", 0xFF7C3AED));
        SERVERS.add(new Server("Binodonmela",   "Binodonmela FTP",      "http://binodonmela.net/", "BM", 0xFFDB2777));
        SERVERS.add(new Server("City Cloud",    "City Cloud FTP",       "http://103.102.253.250/", "CC", 0xFF0284C7));
        SERVERS.add(new Server("iHut FTP",      "iHut Media Server",    "http://103.204.244.70/",  "IH", 0xFFD97706));
        SERVERS.add(new Server("New Hub FTP",   "New Hub Media",        "http://103.14.27.182/",   "NH", 0xFF059669));
        SERVERS.add(new Server("On BDIX",       "On BDIX Media",        "http://onbdix.com/",      "OB", 0xFF4F46E5));
        SERVERS.add(new Server("Boss BD",       "BossBD Media",         "http://www.bossbd.net/",  "BB", 0xFF2563EB));
        SERVERS.add(new Server("Mojaloss",      "Mojaloss Media",       "http://www.mojaloss.net/","ML", 0xFF059669));
        SERVERS.add(new Server("The Intro Vision","Intro Vision FTP",   "http://theintrovision.com/","IV", 0xFFD97706));
        SERVERS.add(new Server("File Server Ebox","Ebox File Server",     "http://fileserver.ebox.live/","FS", 0xFF2563EB));
        SERVERS.add(new Server("IT Base BD",    "IT Base FTP",          "http://itbasebd.net/",    "IB", 0xFF9333EA));
        SERVERS.add(new Server("EN DFNBD",      "EN DFN Network",       "http://em.dfnbd.net/",    "ED", 0xFF0D9488));
        SERVERS.add(new Server("Dhaka Movie",   "Dhaka Movie Server",   "http://www.dhakamovie.com/","DM", 0xFFE11D48));
        SERVERS.add(new Server("Real Movie",    "Real Movie Server",    "http://103.195.1.50/",    "RM", 0xFFD97706));
        SERVERS.add(new Server("Panda Club",    "Panda Club BD",        "http://pandaclubbd.com/", "PC", 0xFF0284C7));
        SERVERS.add(new Server("Asian FTP",     "Asian Media FTP",      "http://asianftp.com/",    "AF", 0xFF4F46E5));
        SERVERS.add(new Server("Meta FTP",      "Meta Media Server",    "http://103.76.196.90/",   "MF", 0xFF059669));
        SERVERS.add(new Server("Genvideos",     "Genvideos Network",    "https://genvideos.org/",  "GV", 0xFFDC2626));
        SERVERS.add(new Server("Mango Gamers",  "Gaming & Media",       "https://mangogamers.com/","MG", 0xFF16A34A));
        SERVERS.add(new Server("BD LAN",        "BD LAN Media Network", "http://www.bdlan.net/",   "BL", 0xFFEA580C));
        SERVERS.add(new Server("SparkNet",      "SparkNet Media",       "https://sparknetbd.com/", "SN", 0xFFDC2626));
        SERVERS.add(new Server("BNet BD",       "BNet Media Server",    "http://www.bnet-bd.com/", "BN", 0xFF0D9488));
        SERVERS.add(new Server("Yes Hub",       "Yes Hub Media",        "http://103.58.73.9/",     "YH", 0xFF7C3AED));
        SERVERS.add(new Server("Media FTPBD",   "FTPBD Media",          "http://media.ftpbd.net/", "MF", 0xFF2563EB));
        SERVERS.add(new Server("FTPBD Server 1","FTPBD Server 1",       "http://server1.ftpbd.net/","F1", 0xFF0284C7));
        SERVERS.add(new Server("FTPBD Server 4","FTPBD Server 4",       "http://server4.ftpbd.net/","F4", 0xFF059669));
        SERVERS.add(new Server("SamBD",         "SamBD Media Network",  "https://sambd.com/",      "SB", 0xFF7C3AED));
        SERVERS.add(new Server("RK Hub 1",      "RK Hub Server 1",      "http://172.16.50.4/",     "R1", 0xFFD97706));
        SERVERS.add(new Server("RK Hub 2",      "RK Hub Server 2",      "http://172.16.50.5/",     "R2", 0xFFEA580C));
        SERVERS.add(new Server("DDnBD",         "DDnBD Entertainment",  "https://ddnbd.com/",      "DD", 0xFF7C3AED));
        SERVERS.add(new Server("DDnBD Fun",     "DDnBD Fun Media",      "http://www.ddnbd.fun/",   "DF", 0xFFDB2777));
        SERVERS.add(new Server("CTG Fun",       "CTG Fun Media",        "http://media.ctgfun.com/","CF", 0xFFDB2777));
        SERVERS.add(new Server("Play Box",      "Play Box Ebox",        "http://play.ebox.live/",  "PB", 0xFF2563EB));
        SERVERS.add(new Server("Circle Network","Circle Network BD",    "https://circlenetworkbd.net/","CN", 0xFF2563EB));
        SERVERS.add(new Server("Circle 2",      "Circle Server 2",      "http://15.1.1.1/",        "C2", 0xFF0284C7));
        SERVERS.add(new Server("MyBD",          "MyBD Media",           "http://15.1.1.4/",        "MB", 0xFF4F46E5));
        SERVERS.add(new Server("Circle 4",      "Circle Server 4",      "http://ftp4.circleftp.net/","C4", 0xFF059669));
        SERVERS.add(new Server("Circle HD",     "Circle HD Server",     "http://hd.circleftp.net/","CH", 0xFFD97706));
        SERVERS.add(new Server("Discovery Net", "Discovery Net BD",     "https://www.discoverynetbd.com/","DN", 0xFF9333EA));
        SERVERS.add(new Server("Discovery IP",  "Discovery Server IP",  "http://103.120.165.196/", "DI", 0xFFDC2626));
        SERVERS.add(new Server("DFlix Discovery","Discovery DFlix",     "http://dflix.discoveryftp.net/","DD", 0xFF7C3AED));
        SERVERS.add(new Server("Discovery CDS1","Discovery CDS1",      "http://cds1.discoveryftp.net/","C1", 0xFF0284C7));
        SERVERS.add(new Server("Discovery CDS2","Discovery CDS2",      "http://cds2.discoveryftp.net/","C2", 0xFF059669));
        SERVERS.add(new Server("Discovery CDS3","Discovery CDS3",      "http://cds3.discoveryftp.net/","C3", 0xFFD97706));
        SERVERS.add(new Server("Khulna Flix",   "Khulna Flix Media",    "http://khulnaflix.net/",  "KF", 0xFF059669));
        SERVERS.add(new Server("File Khulna",   "File Khulna Flix",     "http://file.khulnaflix.net/","FK", 0xFFEA580C));
        SERVERS.add(new Server("Exord Online",  "Exord Online FTP",     "http://www.exordonline.com/","EO", 0xFFD97706));
        SERVERS.add(new Server("RK Box",        "RK Box Server",        "http://103.49.168.107/",  "RB", 0xFF4F46E5));
        SERVERS.add(new Server("ICC Communication","ICC BDIX FTP",      "https://icc.com.bd/",     "IC", 0xFF9333EA));
        SERVERS.add(new Server("ANT BD",        "ANT BD Media",         "http://www.antbd.net/",   "AB", 0xFF0284C7));
        SERVERS.add(new Server("BD Net IP",     "BD Net Server IP",     "http://103.237.37.181/",  "BI", 0xFF16A34A));
    }

    /** @return an unmodifiable view of every configured server, in display order. */
    public static List<Server> all() {
        return Collections.unmodifiableList(SERVERS);
    }

    public static int size() {
        return SERVERS.size();
    }

    public static Server get(int index) {
        return SERVERS.get(index);
    }
}
