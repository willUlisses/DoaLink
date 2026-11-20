import { zodResolver } from '@hookform/resolvers/zod'
import { Plus, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { useFieldArray, useForm, useWatch } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { InfoBanner } from '../../../components/feedback/InfoBanner.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { AddressFieldset } from '../../../components/ui/AddressFieldset.tsx'
import { Button } from '../../../components/ui/Button.tsx'
import { Card } from '../../../components/ui/Card.tsx'
import { Checkbox } from '../../../components/ui/Checkbox.tsx'
import { Input } from '../../../components/ui/Input.tsx'
import { Select } from '../../../components/ui/Select.tsx'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { useToast } from '../../../hooks/useToast.ts'
import { criarOfertaSchema, type CriarOfertaFormValues } from '../../../schemas/oferta.schema.ts'
import { criarOferta } from '../../../services/ofertas.service.ts'
import { TIPO_ITEM_DOACAO, TIPO_UNIDADE } from '../../../types/enums.ts'
import { applyApiErrorsToForm } from '../../../utils/formErrors.ts'
import { LABELS_CATEGORIA_ITEM, LABELS_UNIDADE } from '../../../utils/labels.ts'
import { limparDigitos } from '../../../utils/validators.ts'

const CATEGORIA_OPTIONS = Object.values(TIPO_ITEM_DOACAO).map((c) => ({
  value: c,
  label: LABELS_CATEGORIA_ITEM[c],
}))

const UNIDADE_OPTIONS = Object.values(TIPO_UNIDADE).map((u) => ({
  value: u,
  label: LABELS_UNIDADE[u],
}))

export function NovaOfertaPage() {
  useDocumentTitle('Nova oferta de doação')
  const navigate = useNavigate()
  const { showToast } = useToast()
  const [formError, setFormError] = useState<string | null>(null)

  const {
    register,
    control,
    handleSubmit,
    setValue,
    setError,
    setFocus,
    formState: { errors, isSubmitting },
  } = useForm<CriarOfertaFormValues>({
    resolver: zodResolver(criarOfertaSchema),
    defaultValues: {
      itens: [
        {
          tipoItemDoacao: 'NAO_PERECIVEL',
          tipoUnidade: 'KG',
          descricao: '',
          quantidade: 1,
        },
      ],
      informarEndereco: false,
      endereco: {
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

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'itens',
  })

  const informarEndereco = useWatch({ control, name: 'informarEndereco' })

  const onSubmit = async (values: CriarOfertaFormValues) => {
    setFormError(null)
    try {
      const payload = {
        itens: values.itens.map((item) => ({
          tipoItemDoacao: item.tipoItemDoacao,
          tipoUnidade: item.tipoUnidade,
          descricao: item.descricao.trim(),
          quantidade: item.quantidade,
        })),
        ...(values.informarEndereco
          ? {
              endereco: {
                cep: limparDigitos(values.endereco.cep),
                estado: values.endereco.estado.trim().toUpperCase(),
                cidade: values.endereco.cidade.trim(),
                bairro: values.endereco.bairro.trim(),
                logradouro: values.endereco.logradouro.trim(),
                numero: values.endereco.numero?.trim() || undefined,
                complemento: values.endereco.complemento?.trim() || undefined,
              },
            }
          : {}),
      }

      const criada = await criarOferta(payload)
      showToast({
        variant: 'success',
        title: `Oferta #${criada.id} publicada`,
        description: 'Sua oferta já está visível para reserva por ONGs receptoras.',
      })
      navigate('/app/minhas-ofertas')
    } catch (error) {
      const msg = applyApiErrorsToForm(error, setError, setFocus)
      setFormError(msg)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs
          items={[
            { label: 'Painel', to: '/app' },
            { label: 'Minhas ofertas', to: '/app/minhas-ofertas' },
            { label: 'Nova oferta' },
          ]}
        />
        <h1 className="text-2xl font-bold text-ink">Publicar oferta de doação</h1>
        <p className="mt-1 text-sm text-ink-secondary">
          Adicione de 1 a 50 itens para doação. O local de retirada é opcional.
        </p>
      </div>

      {formError ? <InfoBanner tone="danger">{formError}</InfoBanner> : null}

      <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-6">
        <Card className="space-y-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-base font-bold text-ink">Itens da oferta</h2>
              <p className="text-xs text-ink-secondary">
                Para compatibilidade com um único transporte, mantenha a mesma unidade nos itens da
                oferta quando possível.
              </p>
            </div>
            <Button
              type="button"
              variant="secondary"
              size="sm"
              disabled={fields.length >= 50}
              leftIcon={<Plus className="h-4 w-4" />}
              onClick={() =>
                append({
                  tipoItemDoacao: 'NAO_PERECIVEL',
                  tipoUnidade: 'KG',
                  descricao: '',
                  quantidade: 1,
                })
              }
            >
              Adicionar item
            </Button>
          </div>

          {errors.itens?.root?.message || errors.itens?.message ? (
            <p role="alert" className="text-xs font-medium text-danger">
              {errors.itens?.root?.message ?? errors.itens?.message}
            </p>
          ) : null}

          <div className="space-y-4">
            {fields.map((field, index) => (
              <div
                key={field.id}
                className="rounded-[12px] border border-border bg-canvas p-4 space-y-3"
              >
                <div className="flex items-center justify-between gap-2">
                  <span className="text-xs font-bold uppercase tracking-wide text-ink-secondary">
                    Item #{index + 1}
                  </span>
                  {fields.length > 1 ? (
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => remove(index)}
                      aria-label={`Remover item ${index + 1}`}
                      leftIcon={<Trash2 className="h-3.5 w-3.5 text-danger" />}
                      className="text-danger hover:bg-danger-soft"
                    >
                      Remover
                    </Button>
                  ) : null}
                </div>

                <div className="grid grid-cols-1 gap-3 sm:grid-cols-12">
                  <div className="sm:col-span-4">
                    <Select
                      label="Categoria"
                      options={CATEGORIA_OPTIONS}
                      required
                      error={errors.itens?.[index]?.tipoItemDoacao?.message}
                      {...register(`itens.${index}.tipoItemDoacao`)}
                    />
                  </div>
                  <div className="sm:col-span-4">
                    <Select
                      label="Unidade"
                      options={UNIDADE_OPTIONS}
                      required
                      error={errors.itens?.[index]?.tipoUnidade?.message}
                      {...register(`itens.${index}.tipoUnidade`)}
                    />
                  </div>
                  <div className="sm:col-span-4">
                    <Input
                      label="Quantidade"
                      type="number"
                      step="any"
                      min="0.01"
                      required
                      error={errors.itens?.[index]?.quantidade?.message}
                      {...register(`itens.${index}.quantidade`, {
                        valueAsNumber: true,
                      })}
                    />
                  </div>
                  <div className="sm:col-span-12">
                    <Input
                      label="Descrição do item"
                      placeholder="Ex.: Arroz tipo 1 em pacotes de 5kg"
                      maxLength={500}
                      required
                      error={errors.itens?.[index]?.descricao?.message}
                      {...register(`itens.${index}.descricao`)}
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>
        </Card>

        <Card className="space-y-4">
          <Checkbox
            label="Informar endereço específico para retirada"
            description="Se desmarcado, o local de retirada será combinado fora do sistema após a reserva. Se marcado, preencha todos os campos obrigatórios do endereço."
            {...register('informarEndereco')}
          />

          {informarEndereco ? (
            <AddressFieldset
              prefix="endereco"
              legend="Endereço de retirada"
              description="O endereço completo será ocultado em listagens públicas e exibido apenas para o receptor e transportador vinculados."
              register={register}
              setValue={setValue}
              errors={errors.endereco}
              required
            />
          ) : null}
        </Card>

        <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
          <Button
            type="button"
            variant="secondary"
            onClick={() => navigate('/app/minhas-ofertas')}
          >
            Cancelar
          </Button>
          <Button
            type="submit"
            variant="doador"
            size="lg"
            loading={isSubmitting}
            loadingLabel="Publicando oferta..."
          >
            Publicar oferta
          </Button>
        </div>
      </form>
    </div>
  )
}
