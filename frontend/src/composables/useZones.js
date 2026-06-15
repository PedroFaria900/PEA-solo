// src/composables/useZones.js

const PRECO_POR_ZONA = 1.50

// Base de dados de paragens → zona
// Quando o backend existir, isto vem de GET /api/paragens
export const PARAGENS = {
  // Zona 1
  'Viana Centro':      { zona: 1 },
  'Viana Hospital':    { zona: 1 },
  'Barcelos':          { zona: 1 },
  'Braga Norte':       { zona: 1 },
  'Braga Centro':      { zona: 1 },
  'Guimarães':         { zona: 1 },

  // Zona 2
  'Famalicão':         { zona: 2 },
  'Santo Tirso':       { zona: 2 },
  'Maia':              { zona: 2 },
  'Porto Campanhã':    { zona: 2 },
  'Porto São Bento':   { zona: 2 },

  // Zona 3
  'Gaia':              { zona: 3 },
  'Espinho':           { zona: 3 },
  'Aveiro':            { zona: 3 },
}

/**
 * Calcula o preço de uma viagem entre duas paragens.
 * Regra: paga-se 1.50€ por cada zona atravessada.
 * Zonas são contíguas: de zona 1 a zona 3 → passa 1, 2 e 3 → 4.50€
 *
 * @returns { preco, zonaInicio, zonaFim, zonasAtravessadas, detalhes }
 */
export function calcularPreco(paragemInicio, paragemFim) {
  const inicio = PARAGENS[paragemInicio]
  const fim    = PARAGENS[paragemFim]

  // Se alguma paragem não existe na BD
  if (!inicio || !fim) {
    return { preco: null, erro: 'Paragem não encontrada' }
  }

  const zonaMin = Math.min(inicio.zona, fim.zona)
  const zonaMax = Math.max(inicio.zona, fim.zona)

  // Zonas efectivamente atravessadas (incluindo início e fim)
  const zonasAtravessadas = []
  for (let z = zonaMin; z <= zonaMax; z++) {
    zonasAtravessadas.push(z)
  }

  const preco = zonasAtravessadas.length * PRECO_POR_ZONA

  // Detalhes para mostrar no ecrã de compra
  const detalhes = zonasAtravessadas.map(z => ({
    zona: z,
    preco: PRECO_POR_ZONA
  }))

  return {
    preco,
    zonaInicio: inicio.zona,
    zonaFim:    fim.zona,
    zonasAtravessadas,
    detalhes,
    erro: null
  }
}

/**
 * Devolve todas as paragens de uma zona
 */
export function paragensDaZona(zona) {
  return Object.entries(PARAGENS)
    .filter(([_, v]) => v.zona === zona)
    .map(([nome]) => nome)
}

/**
 * Devolve a zona de uma paragem (ou null se não existir)
 */
export function zonaDeParagem(paragem) {
  return PARAGENS[paragem]?.zona ?? null
}