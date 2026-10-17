# GameUI.gd — HUD, Controlos táteis, Pausa e Ecrã de Fim de Corrida de TRILHO
extends Control

@onready var distance_label: Label = $HUD/DistanceLabel
@onready var coins_label: Label = $HUD/CoinsLabel
@onready var crystals_label: Label = $HUD/CrystalsLabel
@onready var power_up_label: Label = $HUD/PowerUpLabel
@onready var crystal_button: Button = $HUD/CrystalButton

@onready var pause_panel: Control = $PausePanel
@onready var game_over_panel: Control = $GameOverPanel

func _ready() -> void:
	GameManager.score_updated.connect(_on_score_updated)
	GameManager.power_up_activated.connect(_on_power_up_activated)
	GameManager.power_up_ended.connect(_on_power_up_ended)
	GameManager.game_over.connect(_on_game_over)
	
	pause_panel.visible = false
	game_over_panel.visible = false
	power_up_label.visible = false
	
	GameManager.start_game()

func _on_score_updated(distance_meters: int, coins: int, crystals: int) -> void:
	distance_label.text = str(distance_meters) + " m"
	coins_label.text = "Moedas: " + str(coins)
	crystals_label.text = "Cristais: " + str(crystals) + "/3"
	crystal_button.disabled = (crystals <= 0 or GameManager.is_invulnerable)

func _on_power_up_activated(type: String, duration: float) -> void:
	power_up_label.visible = true
	match type:
		"shield":
			power_up_label.text = "ESCUDO ATIVO"
		"magnet":
			power_up_label.text = "ÍMAN ATIVO"
		"boost":
			power_up_label.text = "IMPULSO MÁXIMO"

func _on_power_up_ended(type: String) -> void:
	power_up_label.visible = false

func _on_crystal_button_pressed() -> void:
	GameManager.use_crystal_ability()

func _on_pause_button_pressed() -> void:
	get_tree().paused = true
	pause_panel.visible = true

func _on_resume_pressed() -> void:
	pause_panel.visible = false
	get_tree().paused = false

func _on_restart_pressed() -> void:
	get_tree().paused = false
	get_tree().reload_current_scene()

func _on_menu_pressed() -> void:
	get_tree().paused = false
	get_tree().change_scene_to_file("res://scenes/MainMenu.tscn")

func _on_game_over(distance_meters: int, coins_earned: int, is_new_record: bool) -> void:
	game_over_panel.visible = true
	var text = "Distância: " + str(distance_meters) + " m\nMoedas: " + str(coins_earned)
	if is_new_record:
		text += "\nNOVO RECORDE!"
	else:
		text += "\nRecorde: " + str(SaveManager.best_distance) + " m"
	$GameOverPanel/StatsLabel.text = text

# PONTO DE INTEGRAÇÃO DE ANÚNCIO DE RECOMPENSA (REWARDED AD INTEGRATION POINT):
# func _on_reward_ad_watched() -> void:
#     # Exemplo: AdMob.show_rewarded_video()
#     # Ao receber sucesso:
#     # GameManager.add_crystal()
#     # GameManager.start_game() # ou continuar corrida
#     pass
