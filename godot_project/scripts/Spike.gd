extends Area2D
class_name Spike

func _ready() -> void:
	body_entered.connect(_on_body_entered)

func _on_body_entered(body: Node2D) -> void:
	if body.is_in_group("player"):
		if body.has_method("die"):
			body.die()
	elif body.is_in_group("echo"):
		if body.has_method("destroy"):
			body.destroy()
