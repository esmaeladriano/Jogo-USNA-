# TrackManager.gd — Geração procedural de vias, obstáculos e colecionáveis
extends Node2D

const LANE_X = [180.0, 360.0, 540.0]
var next_spawn_y: float = -200.0
const SPAWN_INTERVAL: float = 380.0

# Tipos de peças de obstáculos:
# 1. Barreira baixa (saltar)
# 2. Barreira alta (deslizar)
# 3. Comboio (mudar de faixa)
# 4. Bloco duplo (ocupa 2 faixas, deixa 1 livre)

var last_free_lane: int = 1

func _process(delta: float) -> void:
	if not GameManager.is_running:
		return
	
	# Mover tudo para baixo com a velocidade do jogo
	var speed = GameManager.current_speed
	for child in get_children():
		child.position.y += speed * delta
		if child.position.y > 1400.0:
			child.queue_free()
	
	next_spawn_y += speed * delta
	if next_spawn_y > 0.0:
		spawn_piece()
		next_spawn_y = -SPAWN_INTERVAL

func spawn_piece() -> void:
	# Escolhe aleatoriamente 1 a 2 faixas para ocupar, garantindo SEMPRE pelo menos 1 faixa livre
	# e que a transição entre a peça anterior e a atual seja sempre fisicamente possível.
	var pattern = randi() % 5
	var free_lane = randi() % 3
	
	# Se a peça anterior tinha uma faixa livre distante (0 para 2), garanta transição suave
	if abs(free_lane - last_free_lane) > 1:
		free_lane = 1 # Centro como intermediário
	last_free_lane = free_lane
	
	match pattern:
		0:
			# Barreira baixa numa faixa aleatória ocupada
			var lane = (free_lane + 1) % 3
			create_obstacle(lane, "low_barrier", -200.0)
			# Moedas na faixa livre
			create_coin_row(free_lane, -200.0, 4)
		1:
			# Barreira alta numa faixa
			var lane = (free_lane + 2) % 3
			create_obstacle(lane, "high_barrier", -200.0)
			create_coin_row(free_lane, -200.0, 3)
		2:
			# Comboio numa faixa
			var lane = (free_lane + 1) % 3
			create_obstacle(lane, "train", -200.0)
			# Possibilidade de cristal ou power-up
			spawn_special_pickup(free_lane, -200.0)
		3:
			# Bloco duplo: ocupa as 2 outras faixas, deixando apenas a free_lane
			for l in range(3):
				if l != free_lane:
					create_obstacle(l, "block", -200.0)
			create_coin_row(free_lane, -200.0, 5)
		4:
			# Comboio + barreira
			var occupied1 = (free_lane + 1) % 3
			var occupied2 = (free_lane + 2) % 3
			create_obstacle(occupied1, "train", -200.0)
			create_obstacle(occupied2, "low_barrier", -200.0)
			create_coin_row(free_lane, -200.0, 4)

func create_obstacle(lane: int, type: String, y_pos: float) -> void:
	var obs = Node2D.new()
	obs.set_script(load("res://scripts/Obstacle.gd"))
	obs.position = Vector2(LANE_X[lane], y_pos)
	obs.setup(type, lane)
	add_child(obs)

func create_coin_row(lane: int, start_y: float, count: int) -> void:
	for i in range(count):
		var coin = Node2D.new()
		coin.set_script(load("res://scripts/Pickup.gd"))
		coin.position = Vector2(LANE_X[lane], start_y - (i * 60.0))
		coin.setup("coin")
		add_child(coin)

func spawn_special_pickup(lane: int, y_pos: float) -> void:
	var roll = randf()
	if roll < 0.15:
		# Cristal raro (stock max 3)
		var crystal = Node2D.new()
		crystal.set_script(load("res://scripts/Pickup.gd"))
		crystal.position = Vector2(LANE_X[lane], y_pos)
		crystal.setup("crystal")
		add_child(crystal)
	elif roll < 0.35:
		# Power-up aleatório
		var types = ["magnet", "shield", "boost"]
		var power_type = types[randi() % types.size()]
		var pu = Node2D.new()
		pu.set_script(load("res://scripts/Pickup.gd"))
		pu.position = Vector2(LANE_X[lane], y_pos)
		pu.setup(power_type)
		add_child(pu)
	else:
		create_coin_row(lane, y_pos, 3)
