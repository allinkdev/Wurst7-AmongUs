package net.wurstclient.commands;

import net.minecraft.client.network.ServerInfo;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.LastServerRememberer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class IpInfoCmd extends Command
{
    public IpInfoCmd()
    {
        super("ipinfo",
                "Shows the IP address information of the server you are currently\n"
                        + "connected to.",
                ".ipinfo");
    }

    @Override
    public void call(String[] args) throws CmdException
    {
        new Thread(() -> {
            String ip = getIP().replaceAll(":([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$", "");
            String realIP = ip;
            try {
                realIP = InetAddress.getByName(ip).toString().replaceAll(ip + "/", "");
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }


            try {
                HttpClient httpClient = HttpClient.newHttpClient();
                System.out.println(String.format("https://get.geojs.io/v1/ip/geo/%s.json", realIP.trim()));
                HttpRequest request = HttpRequest.newBuilder(
                                URI.create(String.format("https://get.geojs.io/v1/ip/geo/%s.json", realIP.trim())))
                        .build();

                CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
                HttpResponse<String> response;

                try {
                    response = responseFuture.get();
                } catch (Exception e) {
                    ChatUtils.message("Unable to get IP address information.");
                    return;
                }

                String[] amogus = response.body().trim().replaceAll("\\{", "\\{\n   ").replaceAll("}", "\n}").replaceAll(",", ",\n  ").split("\n");
                for (String s : amogus) {
                    ChatUtils.message(s);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        /*switch(String.join(" ", args).toLowerCase())
        {
            case "":
                ChatUtils.message("IP: " + ip);
                break;

            case "copy":
                MC.keyboard.setClipboard(ip);
                ChatUtils.message("IP copied to clipboard.");
                break;

            default:
                throw new CmdSyntaxError();
        }*/
    }

    private String getIP()
    {
        ServerInfo lastServer = LastServerRememberer.getLastServer();
        if(lastServer == null || MC.isIntegratedServerRunning())
            return "127.0.0.1:25565";

        String ip = lastServer.address;
        if(!ip.contains(":"))
            ip += ":25565";

        return ip;
    }

    @Override
    public String getPrimaryAction()
    {
        return "Get IP";
    }

    @Override
    public void doPrimaryAction()
    {
        WURST.getCmdProcessor().process("ip");
    }
}
