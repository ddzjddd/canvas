package com.canvas.api;

import com.canvas.config.CryptoService;
import com.canvas.domain.MySqlConnectionEntity;
import com.canvas.domain.VpsHostEntity;
import com.canvas.dto.ResourceDtos;
import com.canvas.repo.MySqlConnectionRepo;
import com.canvas.repo.VpsHostRepo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ResourceController {
    private final MySqlConnectionRepo connRepo;
    private final VpsHostRepo hostRepo;
    private final CryptoService crypto;

    public ResourceController(MySqlConnectionRepo connRepo, VpsHostRepo hostRepo, CryptoService crypto) {
        this.connRepo = connRepo; this.hostRepo = hostRepo; this.crypto = crypto;
    }

    @PostMapping("/mysql-connections")
    public MySqlConnectionEntity createConn(@Valid @RequestBody ResourceDtos.MySqlConnCreate req) {
        MySqlConnectionEntity e = new MySqlConnectionEntity();
        e.setName(req.name()); e.setHost(req.host()); e.setPort(req.port() == null ? 3306 : req.port());
        e.setDatabaseName(req.database()); e.setUsername(req.username()); e.setPasswordEnc(crypto.encrypt(req.password()));
        return connRepo.save(e);
    }

    @GetMapping("/mysql-connections")
    public List<Map<String, Object>> listConns() {
        return connRepo.findAll().stream().map(c -> Map.of(
                "id", c.getId(), "name", c.getName(), "host", c.getHost(), "port", c.getPort(), "database", c.getDatabaseName(),
                "username", c.getUsername(), "dsn_masked", c.getUsername() + "@" + c.getHost() + ":" + c.getPort() + "/" + c.getDatabaseName()
        )).toList();
    }

    @PostMapping("/vps-hosts")
    public VpsHostEntity createHost(@Valid @RequestBody ResourceDtos.VpsHostCreate req) {
        VpsHostEntity e = new VpsHostEntity();
        e.setName(req.name()); e.setHost(req.host()); e.setPort(req.port() == null ? 22 : req.port());
        e.setUsername(req.username()); e.setPrivateKeyEnc(crypto.encrypt(req.privateKey()));
        e.setKnownHostsPolicy(req.knownHostsPolicy() == null ? "accept-new" : req.knownHostsPolicy());
        return hostRepo.save(e);
    }

    @GetMapping("/vps-hosts")
    public List<Map<String, Object>> listHosts() {
        return hostRepo.findAll().stream().map(h -> Map.of(
                "id", h.getId(), "name", h.getName(), "host", h.getHost(), "port", h.getPort(), "username", h.getUsername(),
                "known_hosts_policy", h.getKnownHostsPolicy(), "host_masked", h.getUsername() + "@" + h.getHost() + ":" + h.getPort()
        )).toList();
    }
}
