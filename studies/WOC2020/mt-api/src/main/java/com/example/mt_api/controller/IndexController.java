package com.example.mt_api.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.mt_api.entity.Tenant;
import com.example.mt_api.service.TenantService;
import com.google.gson.Gson;

@Controller
public class IndexController {

    @Autowired
    private TenantService tenantService;

    @GetMapping("/home")
    public String getIndexPage(Model model, @RequestHeader("tenant-id") String tenantName) {

        // System.out.println("Home request reached with tenant id: " + tenantName);

        Tenant t;
        try {
            t = tenantService.getTenant(tenantName);
        } catch (Exception e) {
            t = new Tenant("", "");
        }

        Gson gson = new Gson();

        Map<String, String> user = new HashMap<>();
        user.put("_id", t.getUsers().getFirst().getId().toHexString());
        user.put("name", t.getUsers().getFirst().getName());
        user.put("password", "HIDDEN");
        user.put("tenant", t.getId().toHexString());

        Map<String, String> tenant = new HashMap<>();
        tenant.put("_id", t.getId().toHexString());
        tenant.put("name", t.getName());
        tenant.put("version", t.getVersion());

        model.addAttribute("tenantName", tenantName);
        model.addAttribute("tenantHtml", syntaxHighlight(gson.toJson(tenant)));
        model.addAttribute("userHtml", syntaxHighlight(gson.toJson(user)));

        // Return the name of the template (without the ".html" extension)
        return "home";
    }

    private String syntaxHighlight(String json) {
        // Escape HTML special characters
        json = json.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");

        // Regular expression to match different JSON components
        String regex = "(\"(\\\\u[a-zA-Z0-9]{4}|\\\\[^u]|[^\\\\\"])*\"(\\s*:)?|\\b(true|false|null)\\b|-?\\d+(?:\\.\\d*)?(?:[eE][+\\-]?\\d+)?)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(json);

        StringBuffer highlightedJson = new StringBuffer();

        while (matcher.find()) {
            String match = matcher.group();
            String cls = "number"; // Default class

            if (match.startsWith("\"")) {
                if (match.endsWith(":")) {
                    cls = "key";
                } else {
                    cls = "string";
                }
            } else if ("true".equals(match) || "false".equals(match)) {
                cls = "boolean";
            } else if ("null".equals(match)) {
                cls = "null";
            }

            // Add the match wrapped with a <span> tag and appropriate class
            matcher.appendReplacement(highlightedJson,
                    String.format("<span class=\"%s\">%s</span>", cls, Matcher.quoteReplacement(match)));
        }

        matcher.appendTail(highlightedJson);
        return highlightedJson.toString();
    }
}
