<template>
  <div class="page">

    <!-- ══════════════════════════════════ -->
    <!-- SUCCESS OVERLAY (Green)           -->
    <!-- ══════════════════════════════════ -->
    <transition name="overlay">
      <div v-if="state === 'success'" class="overlay overlay-success">
        <div class="overlay-content">
          <div class="check-circle">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 6 9 17l-5-5"/>
            </svg>
          </div>
          <h2 class="overlay-title">Viagem iniciada</h2>
          <p class="overlay-subtitle">{{ result?.mensagem || 'Título validado com sucesso' }}</p>
          <div class="time-card">
            <div class="time-col">
              <span class="time-label">Validado</span>
              <span class="time-value">{{ validadoHora }}</span>
            </div>
            <div class="time-divider"></div>
            <div class="time-col">
              <span class="time-label">Válido até</span>
              <span class="time-value">{{ validoAteHora }}</span>
            </div>
          </div>
          <p class="auto-close-msg">A fechar em {{ countdown }}s...</p>
        </div>
      </div>
    </transition>

    <!-- ══════════════════════════════════ -->
    <!-- FAILURE OVERLAY (Orange)          -->
    <!-- ══════════════════════════════════ -->
    <transition name="overlay">
      <div v-if="state === 'fail'" class="overlay overlay-fail">
        <button class="back-btn" @click="resetScanner">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M15 18l-6-6 6-6"/>
          </svg>
        </button>
        <div class="overlay-content">
          <div class="x-circle">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#F5A623" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="7" width="18" height="13" rx="2"/>
              <path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"/>
              <line x1="10" y1="11" x2="14" y2="15"/>
              <line x1="14" y1="11" x2="10" y2="15"/>
            </svg>
          </div>
          <h2 class="overlay-title">Sem bilhete válido</h2>
          <p class="overlay-subtitle">Não tem bilhete nem passe ativo para esta zona ou viagem.</p>
          <div class="zone-info-card">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.7)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
            </svg>
            <div>
              <span class="zone-label">TENTATIVA DE VALIDAÇÃO</span>
              <span class="zone-value">{{ failZone }} · Nenhum título</span>
            </div>
          </div>
        </div>
        <button class="buy-btn" @click="$router.push('/comprar-bilhetes')">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#F5A623" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="7" width="20" height="14" rx="2"/>
            <path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"/>
          </svg>
          Comprar bilhete
        </button>
      </div>
    </transition>

    <!-- ══════════════════════════════════ -->
    <!-- MAIN SCANNER PAGE                 -->
    <!-- ══════════════════════════════════ -->
    <header class="header">
      <h1 class="titulo">Validar Bilhete</h1>
      <p class="subtitulo">Aponte a câmara ao QR Code do validador</p>
    </header>

    <!-- Camera / Scanner -->
    <div class="scanner-card">
      <div class="scanner-viewport">
        <!-- Live camera feed -->
        <video ref="videoEl" class="camera-feed" autoplay playsinline muted></video>
        <!-- Canvas used to capture frames (hidden) -->
        <canvas ref="canvasEl" class="scan-canvas"></canvas>

        <!-- Corner brackets overlay -->
        <div class="corner top-left"></div>
        <div class="corner top-right"></div>
        <div class="corner bottom-left"></div>
        <div class="corner bottom-right"></div>

        <!-- Scan line animation -->
        <div class="scan-line" :class="{ scanning: state === 'scanning' }"></div>

        <!-- Camera error -->
        <div v-if="cameraError" class="camera-error">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="1.5"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/><line x1="1" y1="1" x2="23" y2="23" stroke="rgba(255,100,100,0.6)"/></svg>
          <p>{{ cameraError }}</p>
        </div>

        <!-- Processing indicator -->
        <div v-if="state === 'processing'" class="processing-badge">
          <div class="spinner"></div>
          <span>A verificar...</span>
        </div>
      </div>

      <div class="scanner-label">
        <span class="scanner-dot"></span>
        {{ state === 'scanning' ? 'Aponte a câmara ao QR Code' : 'A processar QR Code...' }}
      </div>
    </div>

    <!-- Ticket selector -->
    <div class="ticket-selector">
      <div class="selector-icon">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#0085FF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="2" y="7" width="20" height="14" rx="2"/>
          <path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"/>
        </svg>
      </div>
      <div class="selector-content">
        <span v-if="loading" class="selector-name" style="color:#A3A8B0;">A carregar...</span>
        <span v-else-if="tickets.length === 0" class="selector-name" style="color:#A3A8B0;">Sem títulos ativos</span>
        <select v-else v-model="selectedTicketId" class="ticket-select">
          <option value="" disabled>Selecione um título</option>
          <option v-for="t in tickets" :key="t.id" :value="t.id">{{ ticketLabel(t) }}</option>
        </select>
      </div>
      <span v-if="selectedTicketId" class="ticket-badge">Passe ativo</span>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import jsQR from 'jsqr'

const router = useRouter()
const route = useRoute()

// State machine: 'scanning' | 'processing' | 'success' | 'fail'
const state = ref('scanning')

// Camera refs
const videoEl = ref(null)
const canvasEl = ref(null)
let stream = null
let rafId = null
const cameraError = ref('')

// Tickets
const tickets = ref([])
const selectedTicketId = ref('')
const loading = ref(true)

// Result data
const result = ref(null)
const failZone = ref('')
const countdown = ref(3)
let countdownTimer = null

// Time display
const now = ref(new Date())
const validadoHora = computed(() => now.value.toLocaleTimeString('pt-PT', { hour: '2-digit', minute: '2-digit' }))
const validoAteHora = computed(() => {
  const d = new Date(now.value.getTime() + 60 * 60 * 1000)
  return d.toLocaleTimeString('pt-PT', { hour: '2-digit', minute: '2-digit' })
})

const ticketLabel = (t) => {
  const types = { PASSE: 'Passe', PACK: 'Pack', BILHETE: 'Bilhete' }
  const extra = t.tipo === 'PACK' ? ` [${t.viagensRestantes ?? '?'} viagens]` : ''
  return `${types[t.tipo] || t.tipo} — ${t.areaGeografica || 'Rede Geral'}${extra}`
}

// ─── Camera ──────────────────────────────────────────────────────────────────
const startCamera = async () => {
  try {
    stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'environment', width: { ideal: 640 }, height: { ideal: 640 } }
    })
    videoEl.value.srcObject = stream
    videoEl.value.addEventListener('loadedmetadata', () => {
      videoEl.value.play()
      startScanLoop()
    })
  } catch (e) {
    cameraError.value = 'Sem acesso à câmara. Verifique as permissões.'
  }
}

const stopCamera = () => {
  if (rafId) { cancelAnimationFrame(rafId); rafId = null }
  if (stream) { stream.getTracks().forEach(t => t.stop()); stream = null }
}

// ─── QR Scan Loop ─────────────────────────────────────────────────────────────
const startScanLoop = () => {
  const scan = () => {
    if (state.value !== 'scanning') return
    const video = videoEl.value
    const canvas = canvasEl.value
    if (!video || !canvas || video.readyState < 2) { rafId = requestAnimationFrame(scan); return }

    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    const ctx = canvas.getContext('2d')
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
    const code = jsQR(imageData.data, imageData.width, imageData.height)

    if (code) {
      handleQRCode(code.data)
    } else {
      rafId = requestAnimationFrame(scan)
    }
  }
  rafId = requestAnimationFrame(scan)
}

// ─── Handle scanned QR ────────────────────────────────────────────────────────
const handleQRCode = async (data) => {
  if (state.value !== 'scanning') return
  if (!selectedTicketId.value) return // need a ticket selected

  state.value = 'processing'

  // QR code should contain the leitorId (as plain string or JSON)
  let leitorId
  try {
    const parsed = JSON.parse(data)
    leitorId = parsed.leitorId ?? parsed.id ?? parsed
  } catch {
    leitorId = data // assume the raw value is the leitorId
  }

  try {
    const res = await axios.post('/api/validacoes', {
      tituloId: selectedTicketId.value,
      leitorCodigo: leitorId
    })
    result.value = res.data
    now.value = new Date()

    if (res.data.resultado === 'VALIDO') {
      state.value = 'success'
      startCountdown()
    } else {
      failZone.value = res.data.zona || res.data.areaGeografica || 'Zona desconhecida'
      state.value = 'fail'
    }
  } catch (err) {
    const msg = err.response?.data?.message || ''
    failZone.value = err.response?.data?.zona || 'Zona desconhecida'
    state.value = 'fail'
  }
}

// ─── Countdown to auto-close success screen ───────────────────────────────────
const startCountdown = () => {
  countdown.value = 3
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      resetScanner()
    }
  }, 1000)
}

const resetScanner = () => {
  if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
  result.value = null
  state.value = 'scanning'
  startScanLoop()
}

// ─── Fetch tickets ─────────────────────────────────────────────────────────────
const fetchTickets = async () => {
  try {
    const res = await axios.get('/api/titulos')
    tickets.value = res.data || []
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchTickets()
  if (route.query.ticketId) {
    selectedTicketId.value = route.query.ticketId
  }
  startCamera()
})

onUnmounted(() => {
  stopCamera()
  if (countdownTimer) clearInterval(countdownTimer)
})
</script>

<style scoped>
.page {
  background: #F7F8FA;
  min-height: 100vh;
  padding: 48px 20px 110px;
  font-family: 'Roboto', sans-serif;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* Header */
.header { text-align: center; margin-bottom: 24px; width: 100%; }
.titulo { font-size: 24px; font-weight: 700; color: #0085FF; margin: 0 0 6px; }
.subtitulo { font-size: 13px; color: #6B7077; margin: 0; }

/* Scanner card */
.scanner-card {
  background: #fff;
  border-radius: 24px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.07);
  width: 100%;
  max-width: 340px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
}

.scanner-viewport {
  width: 220px;
  height: 220px;
  background: #111;
  border-radius: 16px;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.camera-feed {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.scan-canvas { display: none; }

/* Corners */
.corner {
  position: absolute;
  width: 28px; height: 28px;
  border-color: #0085FF;
  border-style: solid;
  z-index: 2;
}
.corner.top-left     { top:12px;    left:12px;  border-width: 3px 0 0 3px; border-radius: 4px 0 0 0; }
.corner.top-right    { top:12px;    right:12px; border-width: 3px 3px 0 0; border-radius: 0 4px 0 0; }
.corner.bottom-left  { bottom:12px; left:12px;  border-width: 0 0 3px 3px; border-radius: 0 0 0 4px; }
.corner.bottom-right { bottom:12px; right:12px; border-width: 0 3px 3px 0; border-radius: 0 0 4px 0; }

/* Scan line */
.scan-line {
  position: absolute; left: 12px; right: 12px; height: 2px;
  background: linear-gradient(90deg, transparent, #0085FF, transparent);
  border-radius: 2px; top: 50%; z-index: 2;
}
.scan-line.scanning { animation: scanMove 2.2s ease-in-out infinite; }
@keyframes scanMove {
  0%   { top: 20%; opacity: 0; }
  10%  { opacity: 1; }
  90%  { opacity: 1; }
  100% { top: 80%; opacity: 0; }
}

/* Camera error */
.camera-error {
  position: absolute; inset: 0;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 8px; background: rgba(0,0,0,0.6); text-align: center; padding: 16px;
}
.camera-error p { color: rgba(255,255,255,0.7); font-size: 12px; margin: 0; }

/* Processing badge */
.processing-badge {
  position: absolute; inset: 0; background: rgba(0,0,0,0.55);
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; z-index: 3;
}
.processing-badge span { color: white; font-size: 14px; font-weight: 500; }
.spinner {
  width: 32px; height: 32px;
  border: 3px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Scanner label */
.scanner-label {
  display: flex; align-items: center; gap: 8px;
  font-size: 13px; font-weight: 500; color: #3A3F45;
}
.scanner-dot {
  width: 8px; height: 8px; background: #1F9D4D;
  border-radius: 50%; animation: pulse 1.5s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50%       { opacity: 0.5; transform: scale(0.8); }
}

/* Ticket selector */
.ticket-selector {
  background: #fff; border-radius: 16px; padding: 14px 16px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  display: flex; align-items: center; gap: 14px;
  width: 100%; max-width: 340px; box-sizing: border-box;
}
.selector-icon {
  width: 40px; height: 40px; background: #EAF4FF;
  border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.selector-content { flex: 1; min-width: 0; }
.selector-name { font-size: 15px; font-weight: 600; color: #15171A; }
.ticket-select {
  width: 100%; border: none; outline: none; background: transparent;
  font-size: 15px; font-weight: 600; color: #15171A;
  font-family: 'Roboto', sans-serif; cursor: pointer; appearance: none;
}
.ticket-badge {
  background: #EAF4FF; color: #0085FF; font-size: 12px; font-weight: 600;
  padding: 4px 10px; border-radius: 20px; white-space: nowrap; flex-shrink: 0;
}

/* ═══════════════════════════════════════════════════════════
   OVERLAYS
═══════════════════════════════════════════════════════════ */
.overlay {
  position: fixed; inset: 0; z-index: 200;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  padding: 40px 28px;
  box-sizing: border-box;
}

/* Success - Green */
.overlay-success { background: #2BB467; }

.overlay-content {
  display: flex; flex-direction: column; align-items: center;
  text-align: center; gap: 12px; flex: 1; justify-content: center;
}

.check-circle {
  width: 88px; height: 88px; background: rgba(255,255,255,0.25);
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  margin-bottom: 8px;
}

.overlay-title {
  font-size: 28px; font-weight: 700; color: white; margin: 0;
}
.overlay-subtitle {
  font-size: 16px; color: rgba(255,255,255,0.85); margin: 0;
}

.time-card {
  background: rgba(0,0,0,0.15); border-radius: 16px;
  padding: 16px 32px; display: flex; align-items: center; gap: 28px; margin-top: 8px;
}
.time-col { display: flex; flex-direction: column; gap: 4px; }
.time-label { font-size: 11px; color: rgba(255,255,255,0.7); text-transform: uppercase; letter-spacing: 0.5px; }
.time-value { font-size: 22px; font-weight: 700; color: white; }
.time-divider { width: 1px; height: 40px; background: rgba(255,255,255,0.25); }

.auto-close-msg { font-size: 13px; color: rgba(255,255,255,0.6); margin-top: 8px; }

/* Failure - Orange */
.overlay-fail { background: #F5A623; justify-content: space-between; padding-top: 60px; }

.back-btn {
  position: absolute; top: 20px; left: 20px;
  width: 40px; height: 40px; background: rgba(0,0,0,0.15);
  border: none; border-radius: 12px; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
}

.x-circle {
  width: 80px; height: 80px; background: white;
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  margin-bottom: 8px;
}

.zone-info-card {
  background: rgba(0,0,0,0.12); border-radius: 14px;
  padding: 14px 18px; display: flex; align-items: center; gap: 12px;
  width: 100%; max-width: 300px; text-align: left; margin-top: 8px;
}
.zone-label { display: block; font-size: 10px; color: rgba(255,255,255,0.7); text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 2px; }
.zone-value { font-size: 15px; font-weight: 700; color: white; }

.buy-btn {
  width: 100%; max-width: 320px; height: 56px;
  background: white; border: none; border-radius: 999px;
  font-size: 17px; font-weight: 600; color: #3A3F45;
  font-family: 'Roboto', sans-serif; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 10px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
  margin-bottom: 20px; transition: transform 0.15s;
}
.buy-btn:active { transform: scale(0.97); }

/* Transitions */
.overlay-enter-active, .overlay-leave-active { transition: opacity 0.3s ease, transform 0.3s ease; }
.overlay-enter-from { opacity: 0; transform: scale(1.04); }
.overlay-leave-to   { opacity: 0; transform: scale(0.97); }
</style>
