extends StaticBody2D
class_name Door

@export var linked_button_id: int = 1
@export var open_speed: float = 6.0

var is_open: bool = false
var open_progress: float = 0.0

@onready var collision_shape: CollisionShape2D = $CollisionShape2D
@onready var visual_barrier: Node2D = $Visual

func _ready() -> void:
	add_to_group("doors")
	# Procurar o botão associado
	for btn in get_tree().get_nodes_in_group("buttons"):
		if btn.get("button_id") == linked_button_id:
			btn.state_changed.connect(_on_button_state_changed)
			set_open(btn.is_active())

func _on_button_state_changed(active: bool) -> void:
	set_open(active)

func set_open(opened: bool) -> void:
	is_open = opened

func _process(delta: float) -> void:
	var target = 1.0 if is_open else 0.0
	open_progress = move_toward(open_progress, target, open_speed * delta)

	# Atualizar escala da barreira visual e colisão
	if visual_barrier:
		visual_barrier.scale.y = 1.0 - open_progress
		visual_barrier.visible = open_progress < 0.98

	if collision_shape:
		collision_shape.disabled = open_progress >= 0.95
