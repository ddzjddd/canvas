from jinja2 import Environment, StrictUndefined


env = Environment(undefined=StrictUndefined)


def render_template(value, context: dict):
    if isinstance(value, str):
        return env.from_string(value).render(**context)
    if isinstance(value, dict):
        return {k: render_template(v, context) for k, v in value.items()}
    if isinstance(value, list):
        return [render_template(v, context) for v in value]
    return value
