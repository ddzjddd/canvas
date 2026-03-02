from collections import defaultdict, deque


NODE_DEFS = {
    "mysql_connection_ref": {
        "inputs": {},
        "outputs": {"conn_id": "string", "dsn_masked": "string"},
        "required_config": ["conn_id"],
    },
    "mysql_query": {
        "inputs": {"conn_id": "string"},
        "outputs": {"rows": "array", "row_count": "number", "columns": "array", "elapsed_ms": "number"},
        "required_config": ["sql"],
    },
    "vps_host_ref": {
        "inputs": {},
        "outputs": {"host_id": "string", "host_masked": "string"},
        "required_config": ["host_id"],
    },
    "ssh_command": {
        "inputs": {"host_id": "string"},
        "outputs": {"stdout": "string", "stderr": "string", "exit_code": "number", "elapsed_ms": "number"},
        "required_config": ["cmd"],
    },
}


class ValidationError(Exception):
    pass


def validate_graph(graph: dict) -> None:
    nodes = graph.get("nodes", [])
    edges = graph.get("edges", [])
    node_map = {n["id"]: n for n in nodes}

    for node in nodes:
        n_type = node.get("type")
        if n_type not in NODE_DEFS:
            raise ValidationError(f"Unsupported node type: {n_type}")
        for required in NODE_DEFS[n_type]["required_config"]:
            if required not in node.get("config", {}):
                raise ValidationError(f"Node {node['id']} missing config: {required}")

    for edge in edges:
        from_node = node_map.get(edge["from"]["node"])
        to_node = node_map.get(edge["to"]["node"])
        if not from_node or not to_node:
            raise ValidationError("Edge references unknown nodes")

        out_type = NODE_DEFS[from_node["type"]]["outputs"].get(edge["from"]["port"])
        in_type = NODE_DEFS[to_node["type"]]["inputs"].get(edge["to"]["port"])
        if in_type is None:
            raise ValidationError(f"Invalid input port {edge['to']['port']} for {to_node['id']}")
        if out_type != in_type:
            raise ValidationError(f"Type mismatch: {from_node['id']}.{edge['from']['port']} -> {to_node['id']}.{edge['to']['port']}")

    topological_sort(graph)


def topological_sort(graph: dict) -> list[str]:
    nodes = graph.get("nodes", [])
    edges = graph.get("edges", [])
    indegree = {n["id"]: 0 for n in nodes}
    adj = defaultdict(list)

    for edge in edges:
        src = edge["from"]["node"]
        dst = edge["to"]["node"]
        adj[src].append(dst)
        indegree[dst] += 1

    q = deque([n for n, deg in indegree.items() if deg == 0])
    order = []

    while q:
        cur = q.popleft()
        order.append(cur)
        for nxt in adj[cur]:
            indegree[nxt] -= 1
            if indegree[nxt] == 0:
                q.append(nxt)

    if len(order) != len(nodes):
        raise ValidationError("Graph contains cycle")
    return order
