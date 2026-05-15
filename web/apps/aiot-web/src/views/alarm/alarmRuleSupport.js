export const PROPERTY_OPERATORS = ['GT', 'GTE', 'LT', 'LTE', 'EQ', 'NE', 'BETWEEN']

export function parsePage(raw) {
  const records = Array.isArray(raw?.records) ? raw.records : []
  const total = Number.isFinite(Number(raw?.total)) ? Number(raw.total) : records.length
  return { records, total }
}

export function formatAlarmConditionSummary(row) {
  if (!row) return '—'
  const st = String(row.sourceType || 'PROPERTY').toUpperCase()
  const cj = row.conditionJson
  if (cj == null || cj === '') return '—'
  let o
  try {
    o = typeof cj === 'string' ? JSON.parse(cj) : cj
  } catch {
    const s = String(cj)
    return s.length > 56 ? `${s.slice(0, 53)}…` : s
  }
  if (!o || typeof o !== 'object') return '—'
  if (st === 'EVENT') {
    const et = o.eventType != null ? String(o.eventType) : ''
    const ei = o.eventIdentifier != null ? String(o.eventIdentifier) : ''
    if (!et && !ei) return '—'
    return ei ? `${et} · ${ei}` : et
  }
  const pk = o.propertyKey != null ? String(o.propertyKey) : ''
  const op = o.operator != null ? String(o.operator).toUpperCase() : ''
  const th = o.threshold
  if (!pk && !op) return '—'
  if (op === 'BETWEEN' && th && typeof th === 'object' && 'min' in th && 'max' in th) {
    return `${pk} BETWEEN ${th.min}..${th.max}`
  }
  if (th && typeof th === 'object' && 'value' in th && th.value !== undefined && th.value !== null) {
    return `${pk} ${op} ${th.value}`.replace(/\s+/g, ' ').trim()
  }
  return `${pk} ${op}`.replace(/\s+/g, ' ').trim() || '—'
}

export function createDefaultRuleForm() {
  return {
    deviceGroupKey: '',
    name: '',
    sourceType: 'PROPERTY',
    severity: 'MEDIUM',
    triggerMode: 'INSTANT',
    durationSeconds: 0,
    recoveryMode: 'AUTO',
    dedupMode: 'SINGLE_ACTIVE',
    enabled: true,
    conditionPropertyKey: '',
    conditionOperator: 'GT',
    conditionThresholdValue: '',
    conditionThresholdMin: '',
    conditionThresholdMax: '',
    conditionEventType: '',
    conditionEventIdentifier: ''
  }
}

export function resetConditionFields(ruleForm) {
  ruleForm.conditionPropertyKey = ''
  ruleForm.conditionOperator = 'GT'
  ruleForm.conditionThresholdValue = ''
  ruleForm.conditionThresholdMin = ''
  ruleForm.conditionThresholdMax = ''
  ruleForm.conditionEventType = ''
  ruleForm.conditionEventIdentifier = ''
}

export function hydrateConditionFields(ruleForm, sourceType, conditionJsonRaw) {
  resetConditionFields(ruleForm)
  if (!conditionJsonRaw) return
  let o
  try {
    o = typeof conditionJsonRaw === 'string' ? JSON.parse(conditionJsonRaw) : conditionJsonRaw
  } catch {
    return
  }
  if (!o || typeof o !== 'object') return
  const st = String(sourceType || 'PROPERTY').toUpperCase()
  if (st === 'EVENT') {
    if (o.eventType != null) ruleForm.conditionEventType = String(o.eventType)
    if (o.eventIdentifier != null) ruleForm.conditionEventIdentifier = String(o.eventIdentifier)
    return
  }
  if (o.propertyKey != null) ruleForm.conditionPropertyKey = String(o.propertyKey)
  if (o.operator != null) {
    const op = String(o.operator).toUpperCase()
    ruleForm.conditionOperator = PROPERTY_OPERATORS.includes(op) ? op : 'GT'
  }
  const th = o.threshold
  if (th && typeof th === 'object') {
    if ('value' in th && th.value !== undefined && th.value !== null) {
      ruleForm.conditionThresholdValue = String(th.value)
    }
    if ('min' in th && th.min !== undefined && th.min !== null) {
      ruleForm.conditionThresholdMin = String(th.min)
    }
    if ('max' in th && th.max !== undefined && th.max !== null) {
      ruleForm.conditionThresholdMax = String(th.max)
    }
  }
}

export function buildConditionJson(ruleForm) {
  if (ruleForm.sourceType === 'EVENT') {
    const eventType = String(ruleForm.conditionEventType || '').trim()
    const out = { eventType }
    const ei = String(ruleForm.conditionEventIdentifier || '').trim()
    if (ei) out.eventIdentifier = ei
    return JSON.stringify(out)
  }
  const propertyKey = String(ruleForm.conditionPropertyKey || '').trim()
  const operator = String(ruleForm.conditionOperator || 'GT').toUpperCase()
  if (operator === 'BETWEEN') {
    const min = Number(String(ruleForm.conditionThresholdMin ?? '').trim())
    const max = Number(String(ruleForm.conditionThresholdMax ?? '').trim())
    return JSON.stringify({
      propertyKey,
      operator: 'BETWEEN',
      threshold: { min, max, includeMin: true, includeMax: true }
    })
  }
  const value = Number(String(ruleForm.conditionThresholdValue ?? '').trim())
  return JSON.stringify({ propertyKey, operator, threshold: { value } })
}

/** Device display name + device key in one cell (alarm record list). */
export function formatAlarmRecordObject(row) {
  if (!row) return '—'
  const name = row.deviceName != null ? String(row.deviceName).trim() : ''
  const key = row.deviceKey != null ? String(row.deviceKey).trim() : ''
  if (name && key) return `${name}（${key}）`
  if (name) return name
  if (key) return key
  return '—'
}

function parseAlarmEvidenceJson(raw) {
  if (raw == null || raw === '') return null
  try {
    const o = typeof raw === 'string' ? JSON.parse(raw) : raw
    return o && typeof o === 'object' ? o : null
  } catch {
    return null
  }
}

/** Trigger condition text from alarm record evidence (PROPERTY / EVENT). */
export function formatAlarmRecordTriggerCondition(row) {
  const o = parseAlarmEvidenceJson(row?.evidenceJson)
  if (!o) return '—'
  const st = String(o.sourceType || 'PROPERTY').toUpperCase()
  if (st === 'EVENT') {
    const et = o.eventType != null ? String(o.eventType) : ''
    const ei = o.eventIdentifier != null ? String(o.eventIdentifier) : ''
    if (ei) return `${et} · ${ei}`
    return et || '—'
  }
  const pk = o.propertyKey != null ? String(o.propertyKey) : ''
  const op = o.operator != null ? String(o.operator).toUpperCase() : ''
  const th = o.threshold
  if (!pk && !op) return '—'
  if (op === 'BETWEEN' && th && typeof th === 'object') {
    const min = th.min
    const max = th.max
    if (min !== undefined && max !== undefined) return `${pk} BETWEEN ${min}..${max}`
  }
  if (th && typeof th === 'object' && th.value !== undefined && th.value !== null) {
    return `${pk} ${op} ${th.value}`.replace(/\s+/g, ' ').trim()
  }
  return `${pk} ${op}`.replace(/\s+/g, ' ').trim() || '—'
}

/** Reported / matched trigger value (PROPERTY: numeric value; EVENT: last event hint). */
export function formatAlarmRecordTriggerValue(row) {
  const o = parseAlarmEvidenceJson(row?.evidenceJson)
  if (!o) {
    const lt = row?.lastEventType != null ? String(row.lastEventType).trim() : ''
    return lt || '—'
  }
  const st = String(o.sourceType || 'PROPERTY').toUpperCase()
  if (st === 'EVENT') {
    const lt = row?.lastEventType != null ? String(row.lastEventType).trim() : ''
    const lid = row?.lastEventId != null ? String(row.lastEventId).trim() : ''
    const parts = []
    if (lt) parts.push(lt)
    if (lid) parts.push(`id:${lid.length > 24 ? `${lid.slice(0, 21)}…` : lid}`)
    return parts.length ? parts.join(' · ') : (o.eventType != null ? String(o.eventType) : '—')
  }
  const v = o.value
  if (v === undefined || v === null || v === '') return '—'
  return String(v)
}
