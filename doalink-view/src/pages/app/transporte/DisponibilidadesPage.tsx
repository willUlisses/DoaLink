import { zodResolver } from '@hookform/resolvers/zod'
import { CalendarClock, CheckCircle2, PlusCircle, Trash2 } from 'lucide-react'
import { useCallback, useState } from 'react'
import { useForm } from 'react-hook-form'
import { ConfirmDialog } from '../../../components/feedback/ConfirmDialog.tsx'
import { EmptyState } from '../../../components/feedback/EmptyState.tsx'
import { ErrorState } from '../../../components/feedback/ErrorState.tsx'
import { InfoBanner } from '../../../components/feedback/InfoBanner.tsx'
import { CardListSkeleton } from '../../../components/feedback/Skeleton.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { AddressFieldset } from '../../../components/ui/AddressFieldset.tsx'
import { Button } from '../../../components/ui/Button.tsx'
import { Card } from '../../../components/ui/Card.tsx'
import { Input } from '../../../components/ui/Input.tsx'
import { Select } from '../../../components/ui/Select.tsx'
import { DisponibilidadeCard } from '../../../features/transporte/DisponibilidadeCard.tsx'
import { useAsyncResource } from '../../../hooks/useAsyncResource.ts'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { useToast } from '../../../hooks/useToast.ts'
import {
  criarDisponibilidadeSchema,
  type CriarDisponibilidadeFormValues,
} from '../../../schemas/transporte.schema.ts'
import { normalizeApiError } from '../../../services/api.ts'
import {
  cancelarDisponibilidade,
  criarDisponibilidade,
  encerrarDisponibilidade,
  listarMinhasDisponibilidades,
} from '../../../services/disponibilidades.service.ts'
import { TIPO_UNIDADE, TIPO_VEICULO } from '../../../types/enums.ts'
import type { DisponibilidadeTransporteResponse } from '../../../types/transporte.ts'
import { applyApiErrorsToForm } from '../../../utils/formErrors.ts'
import { obterDataLocalHojeISO } from '../../../utils/formatters.ts'
import { LABELS_UNIDADE, LABELS_VEICULO } from '../../../utils/labels.ts'
import { limparDigitos } from '../../../utils/validators.ts'

const VEICULO_OPTIONS = Object.values(TIPO_VEICULO).map((v) => ({
  value: v,
  label: LABELS_VEICULO[v],
}))

const UNIDADE_OPTIONS = Object.values(TIPO_UNIDADE).map((u) => ({
  value: u,
  label: LABELS_UNIDADE[u],
}))

type PendingAction =
  | { type: 'cancel'; item: DisponibilidadeTransporteResponse }
  | { type: 'close'; item: DisponibilidadeTransporteResponse }

export function DisponibilidadesPage() {
  useDocumentTitle('Disponibilidades de transporte')
  const { showToast } = useToast()

  const [showForm, setShowForm] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)
  const [pendingAction, setPendingAction] = useState<PendingAction | null>(null)
  const [actionLoading, setActionLoading] = useState(false)

  const fetcher = useCallback(() => listarMinhasDisponibilidades(), [])
  const {
    data,
    loading,
    error: loadError,
    reload: loadDisponibilidades,
  } = useAsyncResource(fetcher, [])
  const disponibilidades = data ?? []

  const hojeISO = obterDataLocalHojeISO()

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    setError,
    setFocus,
    formState: { errors, isSubmitting },
  } = useForm<CriarDisponibilidadeFormValues>({
    resolver: zodResolver(criarDisponibilidadeSchema),
    defaultValues: {
      tipoVeiculo: 'UTILITARIO_LEVE',
      capacidade: 100,
      tipoUnidade: 'KG',
      dataDisponivel: hojeISO,
      horarioDisponivel: '',
      origem: {
        cep: '',
        estado: '',
        cidade: '',
        bairro: '',
        logradouro: '',
        numero: '',
        complemento: '',
      },
    },
  })

  const onSubmit = async (values: CriarDisponibilidadeFormValues) => {
    setFormError(null)
    try {
      const criada = await criarDisponibilidade({
        tipoVeiculo: values.tipoVeiculo,
        capacidade: values.capacidade,
        tipoUnidade: values.tipoUnidade,
        dataDisponivel: values.dataDisponivel,
        horarioDisponivel: values.horarioDisponivel?.trim() || undefined,
        origem: {
          cep: limparDigitos(values.origem.cep),
          estado: values.origem.estado.trim().toUpperCase(),
          cidade: values.origem.cidade.trim(),
          bairro: values.origem.bairro.trim(),
          logradouro: values.origem.logradouro.trim(),
          numero: values.origem.numero?.trim() || undefined,
          complemento: values.origem.complemento?.trim() || undefined,
        },
      })

      showToast({
        variant: 'success',
        title: 'Disponibilidade cadastrada',
        description: `Registro #${criada.id} pronto para assumir entregas.`,
      })
      reset()
      setShowForm(false)
      loadDisponibilidades()
    } catch (err) {
      const msg = applyApiErrorsToForm(err, setError, setFocus)
      setFormError(msg)
    }
  }

  const handleConfirmAction = async () => {
    if (!pendingAction) return
    setActionLoading(true)
    try {
      if (pendingAction.type === 'cancel') {
        await cancelarDisponibilidade(pendingAction.item.id)
        showToast({
          variant: 'info',
          title: 'Disponibilidade cancelada',
          description: `Registro #${pendingAction.item.id} marcado como cancelado.`,
        })
      } else {
        await encerrarDisponibilidade(pendingAction.item.id)
        showToast({
          variant: 'success',
          title: 'Disponibilidade encerrada',
          description: `Registro #${pendingAction.item.id} marcado como concluído.`,
        })
      }
      setPendingAction(null)
      loadDisponibilidades()
    } catch (err) {
      showToast({
        variant: 'error',
        title: 'Não foi possível concluir a ação',
        description: normalizeApiError(err).message,
      })
    } finally {
      setActionLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs items={[{ label: 'Painel', to: '/app' }, { label: 'Disponibilidades' }]} />
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-ink">Minhas disponibilidades de transporte</h1>
            <p className="mt-1 text-sm text-ink-secondary">
              Informe veículo, capacidade, origem e data para assumir entregas que aguardam
              transporte. Não há destino fixo no cadastro.
            </p>
          </div>
          <Button
            variant="transportador"
            leftIcon={<PlusCircle className="h-4 w-4" />}
            onClick={() => setShowForm((prev) => !prev)}
          >
            {showForm ? 'Ocultar formulário' : 'Nova disponibilidade'}
          </Button>
        </div>
      </div>

      <InfoBanner tone="info">
        É permitida apenas uma disponibilidade ativa (<strong>DISPONIVEL</strong> ou{' '}
        <strong>RESERVADA</strong>) por data. Não há edição após o cadastro; disponibilidades sem
        vínculos podem ser canceladas.
      </InfoBanner>

      {showForm ? (
        <Card className="border-t-4 border-t-transportador">
          <h2 className="text-lg font-bold text-ink">Cadastrar disponibilidade</h2>
          <p className="mt-1 text-xs text-ink-secondary">
            Os destinos serão definidos automaticamente pelas entregas que você assumir.
          </p>

          {formError ? (
            <div className="mt-4">
              <InfoBanner tone="danger">{formError}</InfoBanner>
            </div>
          ) : null}

          <form onSubmit={handleSubmit(onSubmit)} noValidate className="mt-5 space-y-5">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <Select
                label="Tipo de veículo"
                options={VEICULO_OPTIONS}
                required
                error={errors.tipoVeiculo?.message}
                {...register('tipoVeiculo')}
              />

              <Input
                label="Capacidade"
                type="number"
                step="any"
                min="0.01"
                required
                error={errors.capacidade?.message}
                {...register('capacidade', { valueAsNumber: true })}
              />

              <Select
                label="Unidade da capacidade"
                options={UNIDADE_OPTIONS}
                required
                error={errors.tipoUnidade?.message}
                {...register('tipoUnidade')}
              />

              <Input
                label="Data disponível"
                type="date"
                min={hojeISO}
                required
                error={errors.dataDisponivel?.message}
                {...register('dataDisponivel')}
              />

              <Input
                label="Horário disponível (opcional)"
                type="time"
                error={errors.horarioDisponivel?.message}
                {...register('horarioDisponivel')}
              />
            </div>

            <AddressFieldset
              prefix="origem"
              legend="Endereço de origem"
              description="Ponto de partida do veículo na data informada."
              register={register}
              setValue={setValue}
              errors={errors.origem}
              required
            />

            <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
              <Button type="button" variant="secondary" onClick={() => setShowForm(false)}>
                Cancelar
              </Button>
              <Button
                type="submit"
                variant="transportador"
                loading={isSubmitting}
                loadingLabel="Salvando..."
              >
                Salvar disponibilidade
              </Button>
            </div>
          </form>
        </Card>
      ) : null}

      {loading ? <CardListSkeleton count={3} /> : null}

      {!loading && loadError ? <ErrorState message={loadError.message} onRetry={loadDisponibilidades} /> : null}

      {!loading && !loadError && disponibilidades.length === 0 ? (
        <EmptyState
          icon={<CalendarClock className="h-6 w-6" />}
          title="Nenhuma disponibilidade cadastrada"
          description="Cadastre uma janela de transporte para vincular reservas que aguardam motorista."
          action={
            <Button variant="transportador" onClick={() => setShowForm(true)}>
              Cadastrar disponibilidade
            </Button>
          }
        />
      ) : null}

      {!loading && !loadError && disponibilidades.length > 0 ? (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
          {disponibilidades.map((disp) => (
            <DisponibilidadeCard
              key={disp.id}
              disponibilidade={disp}
              actions={
                <>
                  {disp.situacao === 'DISPONIVEL' ? (
                    <Button
                      variant="destructive"
                      size="sm"
                      leftIcon={<Trash2 className="h-3.5 w-3.5" />}
                      onClick={() => setPendingAction({ type: 'cancel', item: disp })}
                    >
                      Cancelar
                    </Button>
                  ) : null}
                  {disp.situacao === 'RESERVADA' ? (
                    <Button
                      variant="transportador"
                      size="sm"
                      leftIcon={<CheckCircle2 className="h-3.5 w-3.5" />}
                      onClick={() => setPendingAction({ type: 'close', item: disp })}
                    >
                      Encerrar
                    </Button>
                  ) : null}
                </>
              }
            />
          ))}
        </div>
      ) : null}

      <ConfirmDialog
        open={Boolean(pendingAction)}
        title={
          pendingAction?.type === 'cancel'
            ? 'Cancelar disponibilidade?'
            : 'Encerrar disponibilidade?'
        }
        description={
          pendingAction?.type === 'cancel'
            ? `Deseja cancelar a disponibilidade #${pendingAction.item.id}? Essa ação não pode ser desfeita.`
            : `Ao encerrar a disponibilidade #${pendingAction?.item.id ?? ''}, ela será marcada como CONCLUIDA. Todas as entregas vinculadas já devem estar concluídas.`
        }
        confirmLabel={
          pendingAction?.type === 'cancel' ? 'Confirmar cancelamento' : 'Confirmar encerramento'
        }
        confirmVariant={pendingAction?.type === 'cancel' ? 'destructive' : 'transportador'}
        loading={actionLoading}
        onConfirm={handleConfirmAction}
        onClose={() => setPendingAction(null)}
      />
    </div>
  )
}
