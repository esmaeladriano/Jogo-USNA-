# MainMenu.gd — Menu Principal, Loja, Recordes e Definições de TRILHO
extends Control

@onready var main_panel: Control = $MainPanel
@onready var shop_panel: Control = $ShopPanel
@onready var records_panel: Control = $RecordsPanel
@onready var settings_panel: Control = $SettingsPanel

@onready var coins_label: Label = $MainPanel/CoinsLabel
@onready var best_label: Label = $MainPanel/BestLabel

func _ready() -> void:
	update_ui()

func update_ui() -> void:
	coins_label.text = "Moedas: " + str(SaveManager.total_coins)
	best_label.text = "Recorde: " + str(SaveManager.best_distance) + " m"

func _on_play_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/Game.tscn")

func _on_shop_pressed() -> void:
	main_panel.visible = false
	shop_panel.visible = true

func _on_records_pressed() -> void:
	main_panel.visible = false
	records_panel.visible = true
	$RecordsPanel/BestDistanceLabel.text = "Distância Máxima: " + str(SaveManager.best_distance) + " m"
	$RecordsPanel/TotalCoinsLabel.text = "Total de Moedas: " + str(SaveManager.total_coins)

func _on_settings_pressed() -> void:
	main_panel.visible = false
	settings_panel.visible = true
	$SettingsPanel/VibrationCheck.button_pressed = SaveManager.vibration_enabled

func _on_back_to_main_pressed() -> void:
	shop_panel.visible = false
	records_panel.visible = false
	settings_panel.visible = false
	main_panel.visible = true
	update_ui()

func _on_vibration_toggled(button_pressed: bool) -> void:
	SaveManager.vibration_enabled = button_pressed
	SaveManager.save_data()

func _on_buy_skin_pressed(skin_name: String, price: int) -> void:
	if skin_name in SaveManager.unlocked_skins:
		SaveManager.selected_skin = skin_name
		SaveManager.save_data()
	elif SaveManager.total_coins >= price:
		SaveManager.total_coins -= price
		SaveManager.unlocked_skins.append(skin_name)
		SaveManager.selected_skin = skin_name
		SaveManager.save_data()
		update_ui()
