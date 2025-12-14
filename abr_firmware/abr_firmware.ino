// Pin definitions
#define ENA_PIN  2   // Left motor PWM
#define IN1_PIN  3   // Left motor direction
#define IN2_PIN  15  // Left motor direction
#define IN3_PIN  4   // Right motor direction
#define IN4_PIN  5   // Right motor direction
#define ENB_PIN  6   // Right motor PWM

#define PWM_FREQ     5000
#define PWM_RESOLUTION 8
#define DEFAULT_SPEED 200

void setup() {
  Serial.begin(115200);
  pinMode(IN1_PIN, OUTPUT);
  pinMode(IN2_PIN, OUTPUT);
  pinMode(IN3_PIN, OUTPUT);
  pinMode(IN4_PIN, OUTPUT);
  ledcAttach(ENA_PIN, PWM_FREQ, PWM_RESOLUTION);
  ledcAttach(ENB_PIN, PWM_FREQ, PWM_RESOLUTION);
  allStop();
}

void loop() {
  Serial.println("Left FORWARD");
  leftMotorForward(DEFAULT_SPEED);
  delay(2000);
  leftMotorStop();
  delay(500);

  Serial.println("Right FORWARD");
  rightMotorForward(DEFAULT_SPEED);
  delay(2000);
  rightMotorStop();
  delay(500);
}

void leftMotorBackward(int speed) {
  digitalWrite(IN1_PIN, HIGH);
  digitalWrite(IN2_PIN, LOW);
  ledcWrite(ENA_PIN, speed);
}

void leftMotorForward(int speed) {
  digitalWrite(IN1_PIN, LOW);
  digitalWrite(IN2_PIN, HIGH);
  ledcWrite(ENA_PIN, speed);
}

void leftMotorStop() {
  digitalWrite(IN1_PIN, LOW);
  digitalWrite(IN2_PIN, LOW);
  ledcWrite(ENA_PIN, 0);
}

void rightMotorBackward(int speed) {
  digitalWrite(IN3_PIN, HIGH);
  digitalWrite(IN4_PIN, LOW);
  ledcWrite(ENB_PIN, speed);
}

void rightMotorForward(int speed) {
  digitalWrite(IN3_PIN, LOW);
  digitalWrite(IN4_PIN, HIGH);
  ledcWrite(ENB_PIN, speed);
}

void rightMotorStop() {
  digitalWrite(IN3_PIN, LOW);
  digitalWrite(IN4_PIN, LOW);
  ledcWrite(ENB_PIN, 0);
}

void allStop() {
  leftMotorStop();
  rightMotorStop();
}

