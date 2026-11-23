# 📱 Gerador de Notificações Android (.APK)

Aplicativo Android completo desenvolvido em **Kotlin** com **Material Design 3** para simular, criar e testar notificações reais no Android com imagens personalizadas, tipos customizados e temporizador para tela bloqueada.

---

## ✨ Recursos Implementados

1. **Escolha da Imagem**:
   - Selecione qualquer foto ou imagem da galeria do seu celular (`JPG`, `PNG`).
   - A imagem é redimensionada e otimizada automaticamente para evitar travamentos.

2. **Tipos de Notificação Selecionáveis**:
   - 📷 **Banner com Imagem Grande (`BigPictureStyle`)**: Mostra a foto expandida na área de notificações do Android.
   - 💰 **Estilo Banco / Pix**: Formato de alerta de transferência bancária instantânea com subtexto financeiro e destaque.
   - 🔔 **Padrão / Alerta com Foto**: Ícone da foto no perfil e mensagem de texto expansível (`BigTextStyle`).

3. **Temporizador / Delay Inteligente**:
   - Opções: **0s (Imediato)**, **5s**, **10s** ou **30s**.
   - Utiliza `AlarmManager` para disparar a notificação mesmo se você **bloquear a tela do celular** ou sair do aplicativo.

4. **Compatibilidade Total**:
   - Suporte completo ao **Android 13, 14 e 15** com tratamento de permissão `POST_NOTIFICATIONS`.
   - Canal de Notificação com importância alta (`IMPORTANCE_HIGH`), som, vibração e visibilidade em tela de bloqueio (`VISIBILITY_PUBLIC`).

---

## 🛠️ Como Gerar o Arquivo `.APK` no Android Studio

### Passo 1: Abrir o projeto
1. Abra o **Android Studio**.
2. Na tela inicial, clique em **Open** (ou no menu superior `File > Open...`).
3. Navegue até a pasta:
   ```
   C:\Users\blonde\Desktop\operator-development-group-3.0\GeradorNotificacoesAPK
   ```
4. Aguarde o Android Studio sincronizar os arquivos do Gradle (leva menos de 1 minuto na primeira vez).

### Passo 2: Compilar o APK
1. No menu superior do Android Studio, clique em:
   **`Build` > `Build Bundle(s) / APK(s)` > `Build APK(s)`**
2. O Android Studio compilará o aplicativo. Quando concluir, uma notificação aparecerá no canto inferior direito:
   > *"APK(s) generated successfully for module 'app'"* com um link azul **locate**.

3. Clique em **locate** ou vá direto para a pasta:
   ```
   GeradorNotificacoesAPK\app\build\outputs\apk\debug\app-debug.apk
   ```

### Passo 3: Instalar no Celular
1. Envie o arquivo `app-debug.apk` para o seu celular (pode ser via WhatsApp, Telegram, Google Drive ou cabo USB).
2. No celular, toque no arquivo `.apk` para instalar.
3. Se o Android pedir autorização para "Instalar apps desconhecidos", permita e finalize a instalação.
4. Abra o aplicativo **Gerador de Notificações**, conceda a permissão de notificações e aproveite!

---

## ☁️ Alternativa: Gerar o APK direto na Nuvem (GitHub Actions)
Este projeto já inclui o arquivo `.github/workflows/build-apk.yml`. Caso você suba esta pasta para um repositório no seu GitHub, a aba **Actions** compila o `.apk` automaticamente e disponibiliza o download pronto sem precisar do Android Studio!
