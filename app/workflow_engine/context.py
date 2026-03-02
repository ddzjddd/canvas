def build_template_context(run_inputs: dict, node_outputs: dict) -> dict:
    return {
        "inputs": run_inputs,
        **{nid: {"outputs": outputs} for nid, outputs in node_outputs.items()},
    }
