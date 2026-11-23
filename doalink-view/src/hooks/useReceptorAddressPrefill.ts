import { useEffect, useState } from 'react'
import type {
  FieldValues,
  Path,
  PathValue,
  UseFormGetValues,
  UseFormSetValue,
} from 'react-hook-form'
import type { AddressFieldValue } from '../components/ui/AddressFieldset.tsx'
import { obterMeuPerfil } from '../services/usuarios.service.ts'
import { mascararCep } from '../utils/masks.ts'
import { useAuth } from './useAuth.ts'

export function useReceptorAddressPrefill<TFieldValues extends FieldValues>(
  prefix: Path<TFieldValues>,
  setValue: UseFormSetValue<TFieldValues>,
  getValues: UseFormGetValues<TFieldValues>,
) {
  const { user } = useAuth()
  const [loadedForUserId, setLoadedForUserId] = useState<number | null>(null)
  const [hasSavedAddress, setHasSavedAddress] = useState(false)

  useEffect(() => {
    if (!user || user.tipoUsuario !== 'RECEPTOR') return

    let active = true

    obterMeuPerfil()
      .then((perfil) => {
        if (!active) return
        const endereco = perfil.endereco
        if (!endereco || (!endereco.cep && !endereco.logradouro && !endereco.cidade)) {
          setHasSavedAddress(false)
          return
        }

        const atual = getValues(prefix) as AddressFieldValue | undefined
        const preencherSeVazio = (
          campo: keyof AddressFieldValue,
          valor: string | null | undefined,
        ) => {
          if (!valor || valor.trim() === '') return
          const valorAtual = atual?.[campo]
          if (typeof valorAtual === 'string' && valorAtual.trim() !== '') return

          const fieldPath = `${prefix}.${campo}` as Path<TFieldValues>
          setValue(fieldPath, valor as PathValue<TFieldValues, Path<TFieldValues>>, {
            shouldDirty: false,
            shouldValidate: false,
          })
        }

        preencherSeVazio('cep', endereco.cep ? mascararCep(endereco.cep) : '')
        preencherSeVazio('estado', endereco.estado?.toUpperCase() ?? '')
        preencherSeVazio('cidade', endereco.cidade ?? '')
        preencherSeVazio('bairro', endereco.bairro ?? '')
        preencherSeVazio('logradouro', endereco.logradouro ?? '')
        preencherSeVazio('numero', endereco.numero ?? '')
        preencherSeVazio('complemento', endereco.complemento ?? '')

        setHasSavedAddress(true)
      })
      .catch(() => {
        if (!active) return
        setHasSavedAddress(false)
      })
      .finally(() => {
        if (active) setLoadedForUserId(user.id)
      })

    return () => {
      active = false
    }
  }, [user, prefix, setValue, getValues])

  const loadingAddress = user?.tipoUsuario === 'RECEPTOR' && loadedForUserId !== user.id

  return { loadingAddress, hasSavedAddress }
}
