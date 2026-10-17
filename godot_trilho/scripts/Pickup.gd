# Pickup.gd — Entidade de Moedas, Cristais e Power-ups (Íman, Escudo, Impulso)
extends Area2D

var pickup_type: String = "coin"
var player_ref: Node2D = null

func setup(type: String) -> void:
	pickup_type = type

func _ready() -> void:
	body_entered.connect(_on_body_entered)
	draw_visuals()

func _process(delta: float) -> void:
	# Atração magnética para moedas se o íman estiver ativo
	if pickup_type == "coin" and GameManager.magnet_timer > 0.0:
		if not player_ref:
			player_ref = get_tree().get_first_node_in_group("player")
		if player_ref:
			var to_player = player_ref.global_position - global_position
			if to_player.length() < 360.0:
				position += to_player.normalized() * 700.0 * delta

func draw_visuals() -> void:
	var rect = ColorRect.new()
	var col = CollisionShape2D.new()
	var shape = CircleShape2D.new()
	shape.radius = 16.0
	
	match pickup_type:
		"coin":
			rect.size = Vector2(28, 28)
			rect.position = Vector2(-14, -14)
			rect.color = Color(1.0, 0.84, 0.0, 1.0) # Ouro brilhante
		"crystal":
			rect.size = Vector2(32, 32)
			rect.position = Vector2(-16, -16)
			rect.color = Color(0.0, 0.9, 1.0, 1.0) # Ciano elétrico
			shape.radius = 20.0
		"magnet":
			rect.size = Vector2(32, 32)
			rect.position = Vector2(-16, -16)
			rect.color = Color(0.9, 0.2, 0.3, 1.0) # Vermelho íman
			shape.radius = 20.0
		"shield":
			rect.size = Vector2(32, 32)
			rect.position = Vector2(-16, -16)
			rect.color = Color(0.2, 0.85, 0.4, 1.0) # Verde escudo
			shape.radius = 20.0
		"boost":
			rect.size = Vector2(32, 32)
			rect.position = Vector2(-16, -16)
			rect.color = Color(1.0, 0.5, 0.0, 1.0) # Laranja fogo
			shape.radius = 20.0
			
	col.shape = shape
	add_child(rect)
	add_child(col)

func _on_body_entered(body: Node) -> void:
	if not (body is CharacterBody2D):
		return
		
	match pickup_type:
		"coin":
			GameManager.add_coin()
		"crystal":
			GameManager.add_crystal()
		"magnet", "shield", "boost":
			GameManager.activate_power_up(pickup_type)
			
	queue_free()
