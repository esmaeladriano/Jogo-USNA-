# Obstacle.gd — Entidade de obstáculo (Barreira baixa, Barreira alta, Comboio, Bloco)
extends Area2D

var obstacle_type: String = "low_barrier"
var lane_index: int = 1

func setup(type: String, lane: int) -> void:
	obstacle_type = type
	lane_index = lane

func _ready() -> void:
	body_entered.connect(_on_body_entered)
	draw_visuals()

func draw_visuals() -> void:
	var rect = ColorRect.new()
	var col = CollisionShape2D.new()
	var shape = RectangleShape2D.new()
	
	match obstacle_type:
		"low_barrier":
			# Barreira baixa: salta-se por cima (amarelo alaranjado com listas de aviso)
			rect.size = Vector2(90, 36)
			rect.position = Vector2(-45, -36)
			rect.color = Color(1.0, 0.55, 0.1, 1.0)
			shape.size = Vector2(86, 32)
			col.position = Vector2(0, -18)
		"high_barrier":
			# Barreira alta: desliza-se por baixo (azul escuro / ciano metálico alto)
			rect.size = Vector2(90, 70)
			rect.position = Vector2(-45, -120)
			rect.color = Color(0.2, 0.6, 0.9, 1.0)
			shape.size = Vector2(86, 60)
			col.position = Vector2(0, -90)
		"train":
			# Comboio: ocupa a faixa inteira verticalmente (vermelho carmim)
			rect.size = Vector2(100, 240)
			rect.position = Vector2(-50, -240)
			rect.color = Color(0.85, 0.2, 0.25, 1.0)
			shape.size = Vector2(96, 230)
			col.position = Vector2(0, -120)
		"block":
			# Bloco metálico maciço
			rect.size = Vector2(100, 90)
			rect.position = Vector2(-50, -90)
			rect.color = Color(0.5, 0.35, 0.6, 1.0)
			shape.size = Vector2(96, 86)
			col.position = Vector2(0, -45)
			
	col.shape = shape
	add_child(rect)
	add_child(col)

func _on_body_entered(body: Node) -> void:
	if not (body is CharacterBody2D):
		return
	
	# Verificar se o jogador está invulnerável (cristal ou impulso)
	if GameManager.is_invulnerable:
		queue_free()
		return
	
	# Verificar mecânicas específicas de salto e deslize
	if obstacle_type == "low_barrier" and body.is_jumping:
		# Saltou por cima com sucesso!
		return
	
	if obstacle_type == "high_barrier" and body.is_sliding:
		# Deslizou por baixo com sucesso!
		return
	
	# Se tiver escudo temporário ativo, absorve o embate
	if GameManager.has_shield:
		GameManager.has_shield = false
		queue_free()
		return
		
	# Embate fatal: termina a corrida
	GameManager.end_game()
